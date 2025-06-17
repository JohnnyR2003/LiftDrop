package pt.isel.services

import com.example.utils.Either
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import pt.isel.pipeline.pt.isel.liftdrop.DeliveryRequestMessage
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger
import pt.isel.services.courier.CourierService
import pt.isel.services.user.UserService
import java.util.concurrent.ConcurrentHashMap

@Service
class CourierWebSocketHandler(
    private val courierService: CourierService,
    private val userService: UserService,
) : TextWebSocketHandler() {
    private val sessions: MutableMap<Int, WebSocketSession> = ConcurrentHashMap()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session.handshakeHeaders["Authorization"]?.firstOrNull()

        val token = authHeader?.removePrefix("Bearer ")?.trim()

        if (!token.isNullOrBlank()) {
            val courierId = userService.getCourierIdByToken(token)
            if (courierId is Either.Right) {
                val id = courierId.value
                sessions[id] = session
                courierService.startListening(id)
            } else {
                session.close(CloseStatus.NOT_ACCEPTABLE)
            }
        }
    }

    override fun handleTextMessage(
        session: WebSocketSession,
        message: TextMessage,
    ) {
        val json = jacksonObjectMapper().readTree(message.payload)
        when (json.get("type").asText()) {
            "RESPONSE" -> handleCourierResponse(session, json)
            "READY", "NOT_READY" -> toggleCourierAvailability(session)
            else -> println("Unknown message type")
        }
    }

    private fun toggleCourierAvailability(session: WebSocketSession) {
        val courierId = getCourierIdBySession(session)
        if (courierId != null) {
            courierService.startListening(courierId)
        }
    }

    private fun handleCourierResponse(
        session: WebSocketSession,
        json: JsonNode,
    ) {
        val requestId = json.get("requestId").asInt()
        val status = json.get("status").asText()
        val courierId = getCourierIdBySession(session)

        if (courierId != null) {
            when (status) {
                "ACCEPT" -> handleCourierAccept(courierId, requestId)
                "DECLINE" -> handleCourierDecline(courierId, requestId)
            }
        }
    }

    private fun handleCourierAccept(
        courierId: Int,
        requestId: Int,
    ) {
        val accept = courierService.acceptRequest(courierId, requestId)
        if (accept is Either.Left) {
            GlobalLogger.log("Courier $courierId failed to accept request $requestId: ${accept.value}")
            return
        }
        AssignmentCoordinator.complete(requestId, true)
    }

    fun handleCourierDecline(
        courierId: Int,
        requestId: Int,
    ) {
        val decline = courierService.declineRequest(courierId, requestId)
        if (decline is Either.Left) {
            GlobalLogger.log("Courier $courierId failed to decline request $requestId: ${decline.value}")
            return
        }
    }

    private fun getCourierIdBySession(session: WebSocketSession): Int? = sessions.entries.firstOrNull { it.value == session }?.key

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.entries.removeIf { it.value == session }

        val authHeader = session.handshakeHeaders["Authorization"]?.firstOrNull()

        val token = authHeader?.removePrefix("Bearer ")?.trim()
        if (!token.isNullOrBlank()) {
            val id = userService.getCourierIdByToken(token)
            if (id is Either.Right) courierService.stopListening(id.value)
        }
    }

    fun sendDeliveryRequestToCourier(
        courierId: Int,
        request: DeliveryRequestMessage,
    ) {
        val session = sessions[courierId]
        if (session != null && session.isOpen) {
            val payload = jacksonObjectMapper().writeValueAsString(request)
            session.sendMessage(TextMessage(payload))
        }
    }
}
