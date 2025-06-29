package pt.isel.services.client

sealed class RequestCreationError {
    data object RestaurantNotFound : RequestCreationError()

    data object ItemNotFound : RequestCreationError()

    data object ClientNotFound : RequestCreationError()

    data object ClientAddressNotFound : RequestCreationError()

    data object InvalidAddress : RequestCreationError()

    data object InvalidLocation : RequestCreationError()
}

sealed class ClientCreationError {
    data object UserAlreadyExists : ClientCreationError()

    data object InvalidAddress : ClientCreationError()

    data object InvalidLocation : ClientCreationError()
}

sealed class ClientLoginError {
    data object ClientNotFound : ClientLoginError()

    data object InvalidEmailOrPassword : ClientLoginError()

    data object BlankEmailOrPassword : ClientLoginError()

    data object WrongPassword : ClientLoginError()

    data object ClientLoginEmailAlreadyExists : ClientLoginError()

    data object InvalidAddress : ClientLoginError()
}

sealed class ClientRatingError {
    data object RequestNotFound : ClientRatingError()

    data object RatingAlreadyDone : ClientRatingError()
}

sealed class ClientGetRequestStatusError {
    data object RequestNotFound : ClientGetRequestStatusError()
}

sealed class ClientLogoutError {
    data object SessionNotFound : ClientLogoutError()
}

sealed class DropOffCreationError {
    data object ClientNotFound : DropOffCreationError()

    data object InvalidAddress : DropOffCreationError()
}
