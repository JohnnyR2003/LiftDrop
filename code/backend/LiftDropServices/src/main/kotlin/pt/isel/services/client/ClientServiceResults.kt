package pt.isel.services.client

sealed class RequestCreationError {
    data object RestaurantNotFound : RequestCreationError()

    data object ItemNotFound : RequestCreationError()

    data object ClientAddressNotFound : RequestCreationError()
}
