package pt.isel.services.google

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import jakarta.inject.Named
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import liftdrop.repository.TransactionManager
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.liftdrop.*
import pt.isel.liftdrop.DeliveryRequestMessage
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.AssignmentCoordinator
import pt.isel.services.CourierWebSocketHandler
import pt.isel.services.courier.CourierError
import pt.isel.services.courier.LocationUpdateError
import java.util.*

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
        initialMaxDistance: Double = 1000.0,
        maxDistanceIncrement: Double = 1000.0,
        maxAllowedDistance: Double = 4000.0,
        deliveryKind: String,
    ): Boolean {
        val currentMaxDistance = minOf(initialMaxDistance, maxAllowedDistance)
        GlobalLogger.log("Fetching ranked couriers for request ID: $requestId with maxDistance: $currentMaxDistance")
        val rankedCouriers = fetchRankedCouriersByTravelTime(pickupLat, pickupLon, requestId, currentMaxDistance)
        GlobalLogger.log("Ranked couriers: $rankedCouriers")

        if (rankedCouriers is Either.Right) {
            val couriers = rankedCouriers.value

            for (courier in couriers) {
                println("Sent assignment request to courier: ${courier.courierId}")
                val deferredResponse = AssignmentCoordinator.register(requestId)

                transactionManager.run { it ->
                    val requestDetails = it.requestRepository.getRequestForCourierById(requestId)

                    if (requestDetails == null) {
                        GlobalLogger.log("Request details not found for request ID: $requestId")
                        return@run false
                    }

                    val estimatedEarnings =
                        estimateCourierEarnings(
                            distanceKm = courier.distanceMeters / 3600.0, // Convert seconds to hours
                            itemValue = requestDetails.price.toDouble(),
                        )

                    val formattedEarnings = String.format(Locale.US, "%.2f", estimatedEarnings)
                    // Send the assignment request via WebSocket
                    courierWebSocketHandler.sendMessageToCourier(
                        courier.courierId,
                        DeliveryRequestMessage(
                            requestId = requestId,
                            courierId = courier.courierId,
                            pickupLatitude = requestDetails.pickupLocation.latitude,
                            pickupLongitude = requestDetails.pickupLocation.longitude,
                            pickupAddress = requestDetails.pickupAddress,
                            dropoffLatitude = requestDetails.dropoffLocation.latitude,
                            dropoffLongitude = requestDetails.dropoffLocation.longitude,
                            dropoffAddress = requestDetails.dropoffAddress,
                            price = formattedEarnings,
                            deliveryKind = deliveryKind,
                        ),
                    )
                }

                // Wait for the courier’s response or timeout (e.g., 15s)
                val accepted =
                    try {
                        withTimeout(20_000) {
                            deferredResponse.await()
                        }
                    } catch (e: TimeoutCancellationException) {
                        AssignmentCoordinator.complete(requestId, false) // clean up in case it wasn't completed
                        false
                    }

                if (accepted) {
                    return true
                } else {
                    courierWebSocketHandler.handleDeclineRequest(
                        courier.courierId,
                        requestId,
                    )
                }
            }

            // Recursive call with incremented maxDistance, capped at maxAllowedDistance
            return handleCourierAssignment(
                pickupLat,
                pickupLon,
                requestId,
                minOf(currentMaxDistance + maxDistanceIncrement, maxAllowedDistance),
                maxDistanceIncrement,
                maxAllowedDistance,
                deliveryKind,
            )
        } else {
            GlobalLogger.log("No couriers found. Retrying in 5 seconds...")
            delay(10_000) // Wait for 5 seconds before retrying
            return handleCourierAssignment(
                pickupLat,
                pickupLon,
                requestId,
                minOf(currentMaxDistance + maxDistanceIncrement, maxAllowedDistance),
                maxDistanceIncrement,
                maxAllowedDistance,
                deliveryKind,
            )
        }
    }

    private suspend fun fetchRankedCouriersByTravelTime(
        pickupLat: Double,
        pickupLon: Double,
        requestId: Int,
        maxDistance: Double,
    ): Either<CourierError, List<CourierWithLocation>> {
        val nearbyCouriers =
            transactionManager.run {
                val courierRepository = it.courierRepository
                courierRepository.getClosestCouriersAvailable(pickupLat, pickupLon, requestId, maxDistance)
            }

        GlobalLogger.log("Nearby couriers by direct distance: $nearbyCouriers")
        if (nearbyCouriers.isEmpty()) {
            return failure(CourierError.NoAvailableCouriers)
        }

        val couriersWithTravelTime =
            rankCouriersByTravelTime(
                nearbyCouriers,
                pickupLat,
                pickupLon,
            )

        GlobalLogger.log("Ranked couriers by travel time: $couriersWithTravelTime")
        if (couriersWithTravelTime.isEmpty()) {
            return failure(CourierError.NoAvailableCouriers)
        }

        val ranked =
            rankCouriersByScore(
                couriersWithTravelTime,
            )

        GlobalLogger.log("Ranked couriers by combined score: $ranked")
        return success(ranked)
    }

    private suspend fun rankCouriersByTravelTime(
        couriers: List<CourierWithLocation>,
        destinationLat: Double,
        destinationLon: Double,
    ): List<CourierWithLocation> {
        GlobalLogger.log("Ranking couriers by travel time to destination: ($destinationLat, $destinationLon)")
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

    fun rankCouriersByScore(couriers: List<CourierWithLocation>): List<CourierWithLocation> {
        val minTime = couriers.minOf { it.estimatedTravelTime ?: 0L }
        val maxTime = couriers.maxOf { it.estimatedTravelTime ?: 1L }
        val minRating = couriers.minOf { it.rating ?: 0.0 }
        val maxRating = couriers.maxOf { it.rating ?: 5.0 }

        val alpha = 0.7 // weight for travel time
        val beta = 0.3 // weight for rating

        val ranked =
            couriers.sortedBy { courier ->
                val normTime =
                    if (maxTime >
                        minTime
                    ) {
                        ((courier.estimatedTravelTime ?: 0L) - minTime).toDouble() / (maxTime - minTime)
                    } else {
                        0.0
                    }
                val normRating = if (maxRating > minRating) ((courier.rating ?: 0.0) - minRating) / (maxRating - minRating) else 0.0
                alpha * normTime + beta * (1 - normRating)
            }

        return ranked
    }

    fun handleRequestReassignment(
        requestId: Int,
        courierId: Int,
        deliveryStatus: DeliveryStatus,
        pickupLocationDTO: LocationDTO?,
    ): Boolean {
        return transactionManager.run {
            val requestRepository = it.requestRepository
            val requestDetails = requestRepository.getRequestForCourierById(requestId)
            if (requestDetails == null) {
                GlobalLogger.log("Request details not found for request ID: $requestId")
                return@run false
            }

            val (pickupLat, pickupLon) =
                when (deliveryStatus) {
                    DeliveryStatus.HEADING_TO_PICKUP ->
                        requestDetails.pickupLocation.latitude to requestDetails.pickupLocation.longitude
                    DeliveryStatus.HEADING_TO_DROPOFF ->
                        (pickupLocationDTO?.latitude ?: requestDetails.pickupLocation.latitude) to
                            (pickupLocationDTO?.longitude ?: requestDetails.pickupLocation.longitude)
                }

            val pickupPin =
                when (deliveryStatus) {
                    DeliveryStatus.HEADING_TO_PICKUP -> null
                    DeliveryStatus.HEADING_TO_DROPOFF -> requestRepository.getPickupCodeForCancelledRequest(requestId)
                }

            GlobalLogger.log("Reassigning delivery with status $deliveryStatus and kind ${deliveryStatus.toDeliveryKind().name}")

            CoroutineScope(Dispatchers.Default).launch {
                if (handleCourierAssignment(
                        pickupLat = pickupLat,
                        pickupLon = pickupLon,
                        requestId = requestId,
                        deliveryKind = deliveryStatus.toDeliveryKind().name,
                    )
                ) {
                    if (deliveryStatus == DeliveryStatus.HEADING_TO_DROPOFF) {
                        courierWebSocketHandler.sendMessageToCourier(
                            courierId = courierId,
                            message =
                                DeliveryUpdateMessage(
                                    hasBeenAccepted = true,
                                    pinCode = pickupPin ?: "",
                                ),
                        )
                    } else {
                        GlobalLogger.log("Courier $courierId reassigned to request $requestId")
                    }
                }
            }
            return@run true
        }
    }

    fun completeReassignment(requestId: Int) {
        transactionManager.run {
            val courierRepository = it.courierRepository
            val courierId = courierRepository.getCourierIdByCancelledRequest(requestId)
            if (courierId == null) {
                GlobalLogger.log("Courier ID not found for request ID: $requestId")
                return@run
            } else {
                courierWebSocketHandler.sendMessageToCourier(
                    courierId = courierId,
                    message =
                        DeliveryUpdateMessage(
                            hasBeenAccepted = true,
                            hasBeenPickedUp = true,
                        ),
                )
            }
        }
    }

    val baseUrl: String = "https://maps.googleapis.com/maps/api/geocode/json"

    fun reverseGeocode(
        lat: Double,
        lng: Double,
    ): Either<LocationUpdateError, Address> {
        val url = "$baseUrl?latlng=$lat,$lng&key=$apiKey"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response = client.newCall(request).execute()

        val body = response.body?.string() ?: throw Exception("Empty response from geocoding API")

        val json = JsonParser.parseString(body).asJsonObject
        val results = json["results"].asJsonArray
        if (results.size() == 0) return failure(LocationUpdateError.InvalidCoordinates)

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

        val address =
            Address(
                country = country,
                city = city,
                street = street,
                zipCode = zip,
                streetNumber = houseNumber,
                floor = null,
            )

        return success(address)
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

    fun estimateCourierEarnings(
        distanceKm: Double,
        itemValue: Double,
        baseFee: Double = 2.0,
        perKmRate: Double = 0.5,
        valueRate: Double = 0.005,
    ): Double = baseFee + (distanceKm * perKmRate) + (itemValue * valueRate)
}
