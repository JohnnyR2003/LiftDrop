package pt.isel.liftdrop

object Uris {
    const val PREFIX = "/api"

    object User {
        const val BASE = "$PREFIX/user"
        const val ID_BY_TOKEN = "/IdByToken"
    }

    object Client {
        const val BASE = "$PREFIX/client"
        const val MAKE_ORDER = "/makeOrder"
        const val REGISTER = "/register"
        const val LOGIN = "/login"
        const val LOGOUT = "/logout"
        const val CREATE_DROP_OFF_LOCATION = "/createDropOffLocation"
        const val GET_REQUEST_STATUS = "/getRequestStatus/{requestId}"
        const val GIVE_CLASSIFICATION = "/giveClassification"
        const val HELLO = "/hello"
    }

    object Courier {
        const val BASE = "$PREFIX/courier"
        const val REGISTER = "/register"
        const val LOGIN = "/login"
        const val LOGOUT = "/logout"
        const val UPDATE_LOCATION = "/updateLocation"
        const val WAITING_ORDERS = "/waitingOrders"
        const val CANCEL_DELIVERY = "/cancelDelivery"
        const val TRY_PICKUP = "/tryPickup"
        const val TRY_DELIVERY = "/tryDelivery"
        const val PICKED_UP_ORDER = "/pickedUpOrder"
        const val DELIVERED_ORDER = "/deliveredOrder"
        const val FETCH_DAILY_EARNINGS = "/fetchDailyEarnings/{courierId}"
        const val COMPLETE_ORDER = "/completeOrder"
    }

    object Sse {
        const val BASE = "$PREFIX/sse/courier"
        const val STREAM_EVENTS = "/{courierId}"
    }
}
