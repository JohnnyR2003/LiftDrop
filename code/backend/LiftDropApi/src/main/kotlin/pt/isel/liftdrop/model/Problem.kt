package pt.isel.liftdrop.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.net.URI

private const val MEDIA_TYPE = "application/problem+json"

private const val PROBLEM_URI_PATH = "https://github.com/isel-sw-projects/2025-lift-drop/tree/main/docs/liftdrop/problems"

sealed class Problem(
    typeURI: URI,
) {
    val type = typeURI.toString()
    val title = typeURI.toString().split("/").last()

    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .header("Location", type)
            .body(this)

    data object InternalServerError : Problem(URI("$PROBLEM_URI_PATH/internal-server-error"))

    data object CourierAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/courier-already-exists"))

    data object CourierNotFound : Problem(URI("$PROBLEM_URI_PATH/courier-not-found"))

    data object InvalidRestaurantName : Problem(URI("$PROBLEM_URI_PATH/invalid-restaurant-name"))

    data object UserAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/user-already-exists"))

    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object InsecurePassword : Problem(URI("$PROBLEM_URI_PATH/insecure-password"))

    data object InsecureEmail : Problem(URI("$PROBLEM_URI_PATH/insecure-email"))

    data object PasswordIsIncorrect : Problem(URI("$PROBLEM_URI_PATH/password-is-incorrect"))

    // data object TokenExpired : Problem(URI("$PROBLEM_URI_PATH/token-expired"))

    data object TokenNotRevoked : Problem(URI("$PROBLEM_URI_PATH/token-not-revoked"))

    // data object InvalidToken : Problem(URI("$PROBLEM_URI_PATH/invalid-token"))

    data object EmailAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/email-already-exists"))

    data object InvalidRequest : Problem(URI("$PROBLEM_URI_PATH/invalid-request"))

    data object InvalidRequestContent : Problem(URI("$PROBLEM_URI_PATH/invalid-request-content"))

    data object RequestNotFound : Problem(URI("$PROBLEM_URI_PATH/invite-not-found"))

    data object DeliveryCancelled : Problem(URI("$PROBLEM_URI_PATH/delivery-cancelled"))

    data object DeliveryNotFound : Problem(URI("$PROBLEM_URI_PATH/delivery-not-found"))

    companion object {
        fun response(
            status: HttpStatus,
            problem: Problem,
        ): ResponseEntity<Any> =
            ResponseEntity
                .status(status)
                .header("Content-Type", MEDIA_TYPE)
                .body(problem)
    }
}
