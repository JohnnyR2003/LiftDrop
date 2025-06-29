package pt.isel.services

import com.example.utils.Either
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import pt.isel.liftdrop.ResultMessage
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
                "ACCEPT" -> handleAcceptRequest(courierId, requestId)
                "DECLINE" -> handleDeclineRequest(courierId, requestId)
            }
        } else {
            session.close(CloseStatus.NOT_ACCEPTABLE)
        }
    }

    private fun handleAcceptRequest(
        courierId: Int,
        requestId: Int,
    ) {
        val accept = courierService.acceptRequest(courierId, requestId)
        if (accept is Either.Left) {
            handleError("ACCEPT", courierId)
        } else {
            handleSuccess("ACCEPT", requestId, courierId)
        }
    }

    fun handleDeclineRequest(
        courierId: Int,
        requestId: Int,
    ) {
        val decline = courierService.declineRequest(courierId, requestId)
        if (decline is Either.Left) {
            handleError("DECLINE", courierId)
        } else {
            handleSuccess("DECLINE", requestId, courierId)
        }
    }

    fun handleSuccess(
        successKind: String,
        requestId: Int,
        courierId: Int,
    ) {
        when (successKind) {
            "ACCEPT" -> {
                val message = ResultMessage.acceptSuccessMessage()
                sendMessageToCourier(courierId, message)
                AssignmentCoordinator.complete(requestId, true)
            }
            "DECLINE" -> {
                val message = ResultMessage.declineSuccessMessage()
                sendMessageToCourier(courierId, message)
                AssignmentCoordinator.complete(requestId, false)
            }
        }
    }

    fun handleError(
        errorKind: String,
        courierId: Int,
    ) {
        when (errorKind) {
            "ACCEPT" -> {
                val message = ResultMessage.acceptErrorMessage()
                sendMessageToCourier(courierId, message)
            }
            "DECLINE" -> {
                val message = ResultMessage.declineErrorMessage()
                sendMessageToCourier(courierId, message)
            }
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

    fun <T : Any> sendMessageToCourier(
        courierId: Int,
        message: T,
    ) {
        val session = sessions[courierId]
        if (session != null && session.isOpen) {
            val payload = jacksonObjectMapper().writeValueAsString(message)
            session.sendMessage(TextMessage(payload))
        }
    }
}
