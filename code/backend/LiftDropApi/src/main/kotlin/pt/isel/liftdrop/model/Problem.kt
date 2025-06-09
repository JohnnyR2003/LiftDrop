package pt.isel.liftdrop.model

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

private const val MEDIA_TYPE = "application/problem+json"
private const val PROBLEM_URI_PATH = "https://github.com/isel-sw-projects/2025-lift-drop/tree/main/docs/liftdrop/problems"

data class Problem(
    val type: String?,
    val title: String?,
    val status: Int?,
    val detail: String?,
    val instance: String? = null,
) {
    fun response(status: HttpStatus): ResponseEntity<Any> =
        ResponseEntity
            .status(status)
            .header("Content-Type", MEDIA_TYPE)
            .body(this)

    companion object {
        private const val PROBLEM_URI_PATH =
            "https://github.com/isel-sw-projects/2025-lift-drop/tree/main/docs/liftdrop/problems"

        fun internalServerError(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/internal-server-error",
                title = "Internal Server Error",
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                detail = "An unexpected error occurred on the server.",
            )

        fun invalidRequest(detail: String): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/invalid-request",
                title = "Invalid Request",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = detail,
            )

        fun invalidRequestContent(detail: String): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/invalid-request-content",
                title = "Invalid Request Content",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = detail,
            )

        fun courierAlreadyExists(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/courier-already-exists",
                title = "Courier Already Exists",
                status = HttpStatus.CONFLICT.value(),
                detail = "A courier with the given details already exists.",
            )

        fun courierNotNearPickup(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/courier-not-near-pickup",
                title = "Courier Not Near Pickup",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = "You're not near the pickup location.",
            )

        fun courierNotNearDropOff(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/courier-not-near-drop-off",
                title = "Courier Not Near Drop Off",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = "You're not near the drop-off location.",
            )

        fun courierNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/courier-not-found",
                title = "Courier Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested courier could not be found.",
            )

        fun passwordIsIncorrect(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/password-is-incorrect",
                title = "Password Is Incorrect",
                status = HttpStatus.UNAUTHORIZED.value(),
                detail = "The provided password is incorrect.",
            )

        fun packageAlreadyPickedUp(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/package-already-picked-up",
                title = "Package Already Picked Up",
                status = HttpStatus.CONFLICT.value(),
                detail = "The package has already been picked up.",
            )

        fun packageAlreadyDelivered(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/package-already-delivered",
                title = "Package Already Delivered",
                status = HttpStatus.CONFLICT.value(),
                detail = "The package has already been delivered.",
            )

        fun invalidCoordinates(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/invalid-coordinates",
                title = "Invalid Coordinates",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = "The provided coordinates are invalid.",
            )

        fun userAlreadyExists(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/user-already-exists",
                title = "User Already Exists",
                status = HttpStatus.CONFLICT.value(),
                detail = "A user with the given details already exists.",
            )

        fun userNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/user-not-found",
                title = "User Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested user could not be found.",
            )

        fun invalidAddress(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/invalid-address",
                title = "Invalid Address",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = "The provided address is invalid.",
            )

        fun invalidLocation(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/invalid-location",
                title = "Invalid Location",
                status = HttpStatus.BAD_REQUEST.value(),
                detail = "The provided location is invalid.",
            )

        fun restaurantNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/restaurant-not-found",
                title = "Restaurant Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested restaurant could not be found.",
            )

        fun itemNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/item-not-found",
                title = "Item Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested item could not be found.",
            )

        fun clientAddressNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/client-address-not-found",
                title = "Client Address Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested client address could not be found.",
            )

        fun sessionNotFound(): Problem =
            Problem(
                type = "$PROBLEM_URI_PATH/session-not-found",
                title = "Session Not Found",
                status = HttpStatus.NOT_FOUND.value(),
                detail = "The requested session could not be found.",
            )
    }
}
