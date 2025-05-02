package pt.isel.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import pt.isel.pipeline.pt.isel.liftdrop.DeliveryRequestMessage
import java.util.concurrent.ConcurrentHashMap

@Service
class CourierWebSocketHandler(
    private val courierService: CourierService,
) : TextWebSocketHandler() {
    private val sessions: MutableMap<Int, WebSocketSession> = ConcurrentHashMap()

    override fun afterConnectionEstablished(session: WebSocketSession) {
        // Example: Extract courier ID from query or headers
        val courierId =
            session.uri
                ?.query
                ?.substringAfter("courierId=")
                ?.toIntOrNull()
        if (courierId != null) {
            sessions[courierId] = session
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
            courierService.toggleAvailability(courierId)
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

    fun handleCourierAccept(
        courierId: Int,
        requestId: Int,
    ) {
        AssignmentCoordinator.complete(requestId, true)
        courierService.acceptRequest(courierId, requestId)
    }

    fun handleCourierDecline(
        courierId: Int,
        requestId: Int,
    ) {
        AssignmentCoordinator.complete(requestId, false)
        courierService.declineRequest(courierId, requestId)
    }

    private fun getCourierIdBySession(session: WebSocketSession): Int? = sessions.entries.firstOrNull { it.value == session }?.key

    override fun afterConnectionClosed(
        session: WebSocketSession,
        status: CloseStatus,
    ) {
        sessions.entries.removeIf { it.value == session }
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
