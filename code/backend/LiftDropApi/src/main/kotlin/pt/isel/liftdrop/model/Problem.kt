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

    // General Errors
    data object InternalServerError : Problem(URI("$PROBLEM_URI_PATH/internal-server-error"))

    data object InvalidRequest : Problem(URI("$PROBLEM_URI_PATH/invalid-request"))

    data object InvalidRequestContent : Problem(URI("$PROBLEM_URI_PATH/invalid-request-content"))

    // Courier Errors
    data object CourierAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/courier-already-exists"))

    data object CourierNotNearPickup : Problem(URI("$PROBLEM_URI_PATH/courier-not-near-pickup"))

    data object CourierNotFound : Problem(URI("$PROBLEM_URI_PATH/courier-not-found"))

    data object PasswordIsIncorrect : Problem(URI("$PROBLEM_URI_PATH/password-is-incorrect"))

    data object PackageAlreadyPickedUp : Problem(URI("$PROBLEM_URI_PATH/package-already-picked-up"))

    data object PackageAlreadyDelivered : Problem(URI("$PROBLEM_URI_PATH/package-already-delivered"))

    data object InvalidCoordinates : Problem(URI("$PROBLEM_URI_PATH/invalid-coordinates"))

    // Client Errors
    data object UserAlreadyExists : Problem(URI("$PROBLEM_URI_PATH/user-already-exists"))

    data object UserNotFound : Problem(URI("$PROBLEM_URI_PATH/user-not-found"))

    data object InvalidAddress : Problem(URI("$PROBLEM_URI_PATH/invalid-address"))

    data object InvalidLocation : Problem(URI("$PROBLEM_URI_PATH/invalid-location"))

    data object RestaurantNotFound : Problem(URI("$PROBLEM_URI_PATH/restaurant-not-found"))

    data object ItemNotFound : Problem(URI("$PROBLEM_URI_PATH/item-not-found"))

    data object ClientAddressNotFound : Problem(URI("$PROBLEM_URI_PATH/client-address-not-found"))

    data object SessionNotFound : Problem(URI("$PROBLEM_URI_PATH/session-not-found"))

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
