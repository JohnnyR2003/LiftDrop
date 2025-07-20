package pt.isel.services.assignment

import com.example.utils.Either
import com.example.utils.failure
import com.example.utils.success
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
import pt.isel.liftdrop.*
import pt.isel.liftdrop.DeliveryRequestMessage
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.AssignmentCoordinator
import pt.isel.services.CourierWebSocketHandler
import pt.isel.services.courier.CourierError
import java.util.*

const val INITIAL_DISTANCE = 1000.0 // Initial max distance in meters
const val MAX_DISTANCE_INCREMENT = 1000.0 // Incremental distance in meters
const val MAX_ALLOWED_DISTANCE = 4000.0 // Maximum allowed distance in meters
const val COURIER_RESPONSE_TIMEOUT_SECONDS = 20_000L // Timeout for assignment response
const val DELIVERY_RETRY_DELAY_SECONDS = 10_000L // Delay before retrying delivery assignment

@Named("AssignmentServices")
class AssignmentServices(
    private val transactionManager: TransactionManager,
    private val courierWebSocketHandler: CourierWebSocketHandler,
    private val assignmentCoordinator: AssignmentCoordinator,
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
        request: RequestDetailsDTO,
        initialMaxDistance: Double = INITIAL_DISTANCE,
        maxDistanceIncrement: Double = MAX_DISTANCE_INCREMENT,
        maxAllowedDistance: Double = MAX_ALLOWED_DISTANCE,
        deliveryKind: DeliveryKind,
    ): Boolean {
        val currentMaxDistance = minOf(initialMaxDistance, maxAllowedDistance)
        val rankedCouriers = fetchRankedCouriersByTravelTime(pickupLat, pickupLon, request.requestId, currentMaxDistance)

        if (rankedCouriers is Either.Right) {
            val couriers = rankedCouriers.value

            for (courier in couriers) {
                val deferredResponse = assignmentCoordinator.register(request.requestId)

                val estimatedEarnings =
                    estimateCourierEarnings(
                        distanceKm = courier.distanceMeters / 3600.0, // Convert seconds to hours
                        itemValue = request.price.toDouble(),
                        quantity = request.quantity,
                    )

                val formattedEarnings = String.format(Locale.US, "%.2f", estimatedEarnings)
                // Send the assignment request via WebSocket
                courierWebSocketHandler.sendMessageToCourier(
                    courier.courierId,
                    DeliveryRequestMessage(
                        requestId = request.requestId,
                        courierId = courier.courierId,
                        pickupLatitude = request.pickupLocation.latitude,
                        pickupLongitude = request.pickupLocation.longitude,
                        pickupAddress = request.pickupAddress,
                        dropoffLatitude = request.dropoffLocation.latitude,
                        dropoffLongitude = request.dropoffLocation.longitude,
                        dropoffAddress = request.dropoffAddress,
                        item = request.item,
                        quantity = request.quantity,
                        deliveryEarnings = formattedEarnings,
                        deliveryKind = deliveryKind.name,
                    ),
                )

                // Wait for the courier’s response or timeout (e.g., 15s)
                val accepted =
                    try {
                        withTimeout(COURIER_RESPONSE_TIMEOUT_SECONDS) {
                            deferredResponse.await()
                        }
                    } catch (e: TimeoutCancellationException) {
                        false
                    }

                if (accepted) {
                    return true
                }
            }

            // Recursive call with incremented maxDistance, capped at maxAllowedDistance
            return handleCourierAssignment(
                pickupLat,
                pickupLon,
                request,
                minOf(currentMaxDistance + maxDistanceIncrement, maxAllowedDistance),
                maxDistanceIncrement,
                maxAllowedDistance,
                deliveryKind,
            )
        } else {
            delay(DELIVERY_RETRY_DELAY_SECONDS) // Wait for 10 seconds before retrying
            return handleCourierAssignment(
                pickupLat,
                pickupLon,
                request,
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

        if (nearbyCouriers.isEmpty()) {
            return failure(CourierError.NoAvailableCouriers)
        }

        val couriersWithTravelTime =
            rankCouriersByTravelTime(
                nearbyCouriers,
                pickupLat,
                pickupLon,
            )

        if (couriersWithTravelTime.isEmpty()) {
            return failure(CourierError.NoAvailableCouriers)
        }

        val ranked =
            rankCouriersByScore(
                couriersWithTravelTime,
            )

        return success(ranked)
    }

    private suspend fun rankCouriersByTravelTime(
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
                    if (maxTime > minTime) {
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
            val request = requestRepository.getRequestForCourierById(requestId)
            if (request == null) {
                GlobalLogger.log("Request details not found for request ID: $requestId")
                return@run false
            }

            val (pickupLat, pickupLon) =
                when (deliveryStatus) {
                    DeliveryStatus.HEADING_TO_PICKUP ->
                        request.pickupLocation.latitude to request.pickupLocation.longitude
                    DeliveryStatus.HEADING_TO_DROPOFF ->
                        (pickupLocationDTO?.latitude ?: request.pickupLocation.latitude) to
                            (pickupLocationDTO?.longitude ?: request.pickupLocation.longitude)
                }

            val pickupPin =
                when (deliveryStatus) {
                    DeliveryStatus.HEADING_TO_PICKUP -> null
                    DeliveryStatus.HEADING_TO_DROPOFF -> requestRepository.getPickupCodeForCancelledRequest(requestId)
                }

            CoroutineScope(Dispatchers.IO).launch {
                if (handleCourierAssignment(
                        pickupLat = pickupLat,
                        pickupLon = pickupLon,
                        request = request,
                        deliveryKind = deliveryStatus.toDeliveryKind(),
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

    private fun estimateCourierEarnings(
        distanceKm: Double,
        itemValue: Double,
        quantity: Int,
        baseFee: Double = 2.0,
        perKmRate: Double = 0.5,
        valueRate: Double = 0.005,
    ): Double = quantity * (baseFee + (distanceKm * perKmRate) + (itemValue * valueRate))
}
