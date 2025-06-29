package pt.isel.services.google

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
import com.google.gson.JsonParser
import jakarta.inject.Named
import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.liftdrop.Address
import pt.isel.services.courier.LocationUpdateError

@Named("GeocodingServices")
class GeocodingServices {
    /**
     * Base URL for the Google Geocoding API.
     * Make sure to set the GOOGLE_API_KEY environment variable with your API key.
     */
    val baseUrl: String = "https://maps.googleapis.com/maps/api/geocode/json"

    private val apiKey =
        System.getenv()["GOOGLE_API_KEY"]
            ?: throw IllegalStateException("GOOGLE_API_KEY environment variable not set")

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
}
