package pt.isel.services.google

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import jakarta.inject.Named
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import liftdrop.repository.TransactionManager
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.liftdrop.Address
import pt.isel.liftdrop.CourierWithLocation
import pt.isel.pipeline.pt.isel.liftdrop.DeliveryRequestMessage
import pt.isel.services.AssignmentCoordinator
import pt.isel.services.CourierError
import pt.isel.services.CourierWebSocketHandler

@Named("GeocodingServices")
class GeocodingServices(
    private val transactionManager: TransactionManager,
    private val courierWebSocketHandler: CourierWebSocketHandler,
) {
    val httpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        prettyPrint = true
                        isLenient = true
                    },
                )
            }
        }

    private val apiKey =
        System.getenv()["GOOGLE_API_KEY"]
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")

    suspend fun handleCourierAssignment(
        pickupLat: Double,
        pickupLon: Double,
        requestId: Int,
    ) {
        val rankedCouriers = fetchRankedCouriersByTravelTime(pickupLat, pickupLon)
        if (rankedCouriers is Either.Right) {
            val couriers = rankedCouriers.value

            for (courier in couriers) {
                println("Sent assignment request to courier: ${courier.courierId}")
                val deferredResponse = AssignmentCoordinator.register(requestId)

                transactionManager.run { it ->
                    val requestDetails = it.requestRepository.getRequestForCourierById(requestId)

                    // Send the assignment request via WebSocket
                    courierWebSocketHandler.sendDeliveryRequestToCourier(
                        courier.courierId,
                        DeliveryRequestMessage(
                            requestId = requestId,
                            pickupLatitude = requestDetails.pickupLocation.latitude,
                            pickupLongitude = requestDetails.pickupLocation.longitude,
                            pickupAddress = requestDetails.pickupAddress,
                            dropoffLatitude = requestDetails.dropoffLocation.latitude,
                            dropoffLongitude = requestDetails.dropoffLocation.longitude,
                            dropoffAddress = requestDetails.dropoffAddress,
                            price = requestDetails.price,
                        ),
                    )
                }

                // Wait for the courier’s response or timeout (e.g., 15s)
                val accepted =
                    try {
                        withTimeout(15_000) {
                            deferredResponse.await()
                        }
                    } catch (e: TimeoutCancellationException) {
                        false
                    }

                if (accepted) {
                    // Successfully assigned
                    break
                }
            }
        }
    }

    private suspend fun fetchRankedCouriersByTravelTime(
        pickupLat: Double,
        pickupLon: Double,
    ): Either<CourierError, List<CourierWithLocation>> {
        val nearbyCouriers =
            transactionManager.run {
                val courierRepository = it.courierRepository
                courierRepository.getClosestCouriersAvailable(pickupLat, pickupLon)
            }

        if (nearbyCouriers.isEmpty()) {
            return failure(CourierError.NoAvailableCouriers)
        }

        val ranked =
            rankCouriersByTravelTime(
                nearbyCouriers,
                pickupLat,
                pickupLon,
            )

        return success(ranked)
    }

    suspend fun rankCouriersByTravelTime(
        couriers: List<CourierWithLocation>,
        destinationLat: Double,
        destinationLon: Double,
    ): List<CourierWithLocation> {
        val origins =
            couriers.joinToString("|") {
                "${it.latitude},${it.longitude}"
            }
        val destination = "$destinationLat,$destinationLon"

        val response =
            httpClient.get("https://maps.googleapis.com/maps/api/distancematrix/json") {
                parameter("origins", origins)
                parameter("destinations", destination)
                parameter("key", apiKey)
            }

        val body = response.bodyAsText()
        val json = Json.parseToJsonElement(body).jsonObject

        val travelTimes =
            json["rows"]?.jsonArray?.mapNotNull { row ->
                row.jsonObject["elements"]
                    ?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject
                    ?.get(
                        "duration",
                    )?.jsonObject
                    ?.get("value")
                    ?.jsonPrimitive
                    ?.longOrNull
            } ?: emptyList()

        return couriers.zip(travelTimes).sortedBy { it.second }.map { (courier, time) ->
            courier.copy(estimatedTravelTime = time)
        }
    }

    val baseUrl: String = "https://maps.googleapis.com/maps/api/geocode/json"

    fun reverseGeocode(
        lat: Double,
        lng: Double,
    ): Address {
        val url = "$baseUrl?latlng=$lat,$lng&key=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: throw Exception("Empty response from geocoding API")

        val json = JsonParser.parseString(body).asJsonObject
        val results = json["results"].asJsonArray
        println("Geocoding API raw response: $body")
        println("Geocoding API parsed response: $json")
        if (results.size() == 0) throw Exception("No address found")

        val addressComponents = results[0].asJsonObject["address_components"].asJsonArray

        var country = ""
        var city = ""
        var street = ""
        var zip = ""
        var houseNumber = ""

        for (component in addressComponents) {
            val types = component.asJsonObject["types"].asJsonArray.map { it.asString }
            when {
                "country" in types -> country = component.asJsonObject["long_name"].asString
                "locality" in types || "administrative_area_level_2" in types -> city = component.asJsonObject["long_name"].asString
                "route" in types -> street = component.asJsonObject["long_name"].asString
                "postal_code" in types -> zip = component.asJsonObject["long_name"].asString
                "street_number" in types -> houseNumber = component.asJsonObject["long_name"].asString
            }
        }

        return Address(
            country = country,
            city = city,
            street = street,
            zipCode = zip,
            streetNumber = houseNumber,
            floor = null,
        )
    }

    fun getLatLngFromAddress(address: String): Pair<Double, Double>? {
        val client = OkHttpClient()
        println(address)
        val url =
            baseUrl +
                "?address=${address.replace(" ", "+")}&key=$apiKey"
        println(url)
        val request =
            Request
                .Builder()
                .url(url)
                .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Unexpected code $response")

            val body = response.body?.string() ?: throw Exception("Empty response from geocoding API")
            val json = JsonParser.parseString(body).asJsonObject

            if (json["status"].asString == "ZERO_RESULTS") {
                return null
            }

            if (json["status"].asString == "OK") {
                val results = json["results"].asJsonArray
                if (results.size() > 0) {
                    val location = results[0].asJsonObject["geometry"].asJsonObject["location"].asJsonObject
                    val lat = location["lat"].asDouble
                    val lng = location["lng"].asDouble
                    return Pair(lat, lng)
                } else {
                    throw Exception("No results found for address: $address")
                }
            } else {
                throw Exception("Error from geocoding API: ${json["status"].asString}")
            }
        }
    }
}
