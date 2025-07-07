package pt.isel.liftdrop.shared.model

object Uris {
    const val PREFIX = "/api"

    object User {
        const val BASE = "$PREFIX/user"
        const val ID_BY_TOKEN = "$BASE/IdByToken"
    }

    object Courier {
        const val BASE = "$PREFIX/courier"
        const val REGISTER = "$BASE/register"
        const val LOGIN = "$BASE/login"
        const val LOGOUT = "$BASE/logout"
        const val UPDATE_LOCATION = "$BASE/updateLocation"
        const val CANCEL_DELIVERY = "$BASE/cancelDelivery"
        const val TRY_PICKUP = "$BASE/tryPickup"
        const val TRY_DELIVERY = "$BASE/tryDelivery"
        const val PICKED_UP_ORDER = "$BASE/pickedUpOrder"
        const val DELIVERED_ORDER = "$BASE/deliveredOrder"
        const val FETCH_DAILY_EARNINGS = "$BASE/fetchDailyEarnings/{courierId}"
    }
}
