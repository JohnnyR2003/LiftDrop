package pt.isel.liftdrop.home.model

import okhttp3.OkHttpClient
import okhttp3.Request
import pt.isel.liftdrop.services.http.Result
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import pt.isel.liftdrop.HOST
import pt.isel.liftdrop.services.http.HttpService
import pt.isel.liftdrop.services.http.Problem
import pt.isel.liftdrop.shared.model.Uris

interface HomeService {
    suspend fun startListening(token: String, onMessage: (String) -> Unit, onFailure: (Throwable) -> Unit)

    suspend fun stopListening()

    suspend fun acceptRequest(requestId: String, token: String): Boolean

    suspend fun declineRequest(requestId: String): Boolean

    suspend fun validatePickup(requestId: String, courierId: String, token: String): Result<Boolean>

    suspend fun pickupOrder(requestId: String, courierId: String, pickUpPin: String, deliveryKind: String, token: String): Result<Boolean>

    suspend fun validateDropOff(requestId: String, courierId: String, token: String): Result<Boolean>

    suspend fun deliverOrder(requestId: String, courierId: String, dropOffPin: String, deliveryEarnings: Double, token: String): Result<Boolean>

    suspend fun cancelDelivery(courierId: String, requestId: String, deliveryStatus: String, pickUpLocation: LocationDTO? = null, token: String): Result<Boolean>

    suspend fun getDailyEarnings(courierId: String, token: String): Result<Double>

    suspend fun getCourierIdByToken(token: String): Result<Int>
}

class RealHomeService(
    private val httpService: HttpService,
) : HomeService {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    private var isConnected = false

    override suspend fun startListening(
        token: String,
        onMessage: (String) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        if (isConnected) return

        val request = Request.Builder()
            .url("$HOST/ws/courier")
            .addHeader("Authorization", "Bearer $token")
            .build()


        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                println("WebSocket connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onFailure(t)
                isConnected = false
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
                isConnected = false
                println("WebSocket closing: $code / $reason")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                println("WebSocket closed: $code / $reason")
            }
        })
    }

    override suspend fun stopListening() {
        webSocket?.close(1000, "Client stopped listening")
        webSocket = null
        isConnected = false
    }

    override suspend fun acceptRequest(requestId: String, token: String): Boolean {

        val messageJson = """
    {
        "type": "DECISION",
        "requestId": "$requestId",
        "decision": "ACCEPT"
    }
""".trimIndent()

        return webSocket?.send(messageJson) == true
    }

    override suspend fun declineRequest(requestId: String): Boolean {
        val messageJson = """
    {
        "type": "DECISION",
        "requestId": "$requestId",
        "decision": "DECLINE"
    }
""".trimIndent()

        return webSocket?.send(messageJson) == true
    }

    override suspend fun validatePickup(requestId: String, courierId: String, token: String): Result<Boolean> {
        val body = ValidatePickupInputModel(
            requestId = requestId.toInt(),
            courierId = courierId.toInt()
        )

        return httpService.post<ValidatePickupInputModel, Boolean>(
            url = Uris.Courier.TRY_PICKUP,
            data = body,
            token = token
        )
    }

    override suspend fun pickupOrder(requestId: String, courierId: String, pickUpPin: String, deliveryKind: String, token: String): Result<Boolean> {
        val body = PickupOrderInputModel(
            requestId = requestId.toInt(),
            courierId = courierId.toInt(),
            pickupCode = pickUpPin,
            deliveryKind = deliveryKind // Assuming a default value for deliveryKind
        )

        return httpService.post<PickupOrderInputModel, Boolean>(
            url = Uris.Courier.PICKED_UP_ORDER,
            data = body,
            token = token
        )
    }

    override suspend fun validateDropOff(requestId: String, courierId: String, token: String): Result<Boolean> {
        val body = ValidateDropOffInputModel(
            requestId = requestId.toInt(),
            courierId = courierId.toInt()
        )

        return httpService.post<ValidateDropOffInputModel, Boolean>(
            url = Uris.Courier.TRY_DELIVERY,
            data = body,
            token = token
        )
    }

    override suspend fun deliverOrder(
        requestId: String,
        courierId: String,
        dropOffPin: String,
        deliveryEarnings: Double,
        token: String
    ): Result<Boolean> {
        val body = DeliverOrderInputModel(
            requestId = requestId.toInt(),
            courierId = courierId.toInt(),
            dropoffCode = dropOffPin,
            deliveryEarnings = deliveryEarnings
        )

        return httpService.post<DeliverOrderInputModel, Boolean>(
            url = Uris.Courier.DELIVERED_ORDER,
            data = body,
            token = token
        )
    }

    override suspend fun cancelDelivery(
        courierId: String,
        requestId: String,
        deliveryStatus: String,
        pickUpLocation: LocationDTO?,
        token: String
    ): Result<Boolean> {
        val body = CancelDeliveryInputModel(
            courierId = courierId.toInt(),
            requestId = requestId.toInt(),
            deliveryStatus = deliveryStatus,
            pickupLocation = pickUpLocation
        )

        return httpService.post<CancelDeliveryInputModel, Boolean>(
            url = Uris.Courier.CANCEL_DELIVERY,
            data = body,
            token = token
        )
    }

    override suspend fun getDailyEarnings(courierId: String, token: String): Result<Double> {
        return httpService.get<Double>(
            url = Uris.Courier.FETCH_DAILY_EARNINGS.replace("{courierId}", courierId),
            token = token
        )
    }

    override suspend fun getCourierIdByToken(token: String): Result<Int> {
        return httpService.get(
            url = Uris.User.ID_BY_TOKEN,
            token = token
        )
    }
}