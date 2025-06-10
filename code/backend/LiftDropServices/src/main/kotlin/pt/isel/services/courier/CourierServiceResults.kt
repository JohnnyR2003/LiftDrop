package pt.isel.services.courier

sealed class CourierCreationError {
    data object CourierEmailAlreadyExists : CourierCreationError()
}

sealed class CourierLoginError {
    data object BlankEmailOrPassword : CourierLoginError()

    data object CourierNotFound : CourierLoginError()

    data object InvalidEmailOrPassword : CourierLoginError()

    data object WrongPassword : CourierLoginError()
}

sealed class CourierEarningsError {
    data object CourierNotFound : CourierEarningsError()
}

sealed class CourierLogoutError {
    data object SessionNotFound : CourierLogoutError()
}

sealed class LocationUpdateError {
    data object InvalidCoordinates : LocationUpdateError()

    data object CourierNotFound : LocationUpdateError()
}

sealed class StateUpdateError {
    data object CourierNotFound : StateUpdateError()
}

sealed class CourierDeliveryError {
    data object CourierNotNearPickup : CourierDeliveryError()

    data object PackageAlreadyPickedUp : CourierDeliveryError()

    data object CourierNotNearDropOff : CourierDeliveryError()

    data object PackageAlreadyDelivered : CourierDeliveryError()
}

sealed class CourierCancelDeliveryError {
    data object PackageAlreadyDelivered : CourierCancelDeliveryError()
}

sealed class CourierError {
    data object CourierNotFound : CourierError()

    data object RequestNotAccepted : CourierError()

    data object NoAvailableCouriers : CourierError()

    data object NoCourierAvailable : CourierError()
}
