package pt.isel.liftdrop.home.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.isel.liftdrop.home.ui.HomeScreenState
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.PreferencesRepository
import pt.isel.liftdrop.services.LocationTrackingService
import pt.isel.liftdrop.services.http.Problem
import pt.isel.liftdrop.services.http.Result

class HomeViewModel(
    private val homeService: HomeService,
    private val loginService: LoginService,
    private val locationServices: LocationTrackingService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    private val _currentRequest : MutableStateFlow<CourierRequestDetails?> =
        MutableStateFlow(null)

    private val _stateFlow: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Idle("0.00"))

    private val _previousState: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Idle("0.00"))

    val currentRequest: StateFlow<CourierRequestDetails?>
        get() = _currentRequest.asStateFlow()
    val stateFlow: StateFlow<HomeScreenState> = _stateFlow.asStateFlow()
    val previousState: StateFlow<HomeScreenState> = _previousState.asStateFlow()

    val dailyEarnings: Flow<String>
        get() = _dailyEarnings.asStateFlow()

    val deliveryStatus: Flow<String>
        get() = _stateFlow.map { state ->
            when (state) {
                is HomeScreenState.HeadingToPickUp -> "HEADING_TO_PICKUP"
                is HomeScreenState.HeadingToDropOff -> "HEADING_TO_DROPOFF"
                is HomeScreenState.PickedUp -> "PICKED_UP"
                is HomeScreenState.Delivered -> "DELIVERED"
                else -> "IDLE"
            }
        }

    val _dailyEarnings: MutableStateFlow<String> = MutableStateFlow("0.00")

    val _serviceStarted = MutableStateFlow<Boolean>(false)
    val serviceStarted: StateFlow<Boolean> = _serviceStarted.asStateFlow()

    fun handlePermissions(
        permissions: Map<String, Boolean>,
        startLocationService: (authToken: String, courierId: String) -> Unit
    ) {
        viewModelScope.launch {
            val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
            val fgLocationGranted =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    permissions[Manifest.permission.FOREGROUND_SERVICE_LOCATION] ?: false
                } else true

            if ((fineGranted || coarseGranted) && fgLocationGranted) {
                val userInfo = preferences.getUserInfo()
                if (userInfo != null) {
                    startLocationService(userInfo.bearer, userInfo.id.toString())
                    _serviceStarted.value = true
                }
            }
        }
    }

    fun updateState(newState: HomeScreenState): HomeScreenState {
        _previousState.value = _stateFlow.value // Salva o estado atual como o anterior
        _stateFlow.value = newState // Atualiza o estado atual
        return newState // Retorna o novo estado
    }

    fun dismissError() {
        _stateFlow.value = previousState.value
    }

    fun logout() {
        viewModelScope.launch {
            val userInfo = preferences.getUserInfo()
            if (userInfo != null) {
                preferences.clearUserInfo(userInfo)
                val result = loginService.logout(
                    userInfo.bearer,
                    userInfo.id.toString()
                )

                if (result is Result.Error) {
                    updateState(HomeScreenState.Error(result.problem))
                } else {
                    preferences.clearUserInfo(userInfo)
                    updateState(HomeScreenState.Logout(true))
                }
            } else {
                updateState(
                    HomeScreenState.Error(
                        Problem(
                            type = "LogoutError",
                            title = "Logout Error",
                            detail = "You are not logged in.",
                            status = 403
                        )
                    )
                )
            }
        }
    }

    fun resetToListeningState() {
        check(_stateFlow.value !is HomeScreenState.Listening) {
            "The view model is already in the idle state."
        }
        _stateFlow.update { current ->
            when (current) {
                is HomeScreenState.Delivered-> HomeScreenState.Listening(
                    dailyEarnings = current.dailyEarnings,
                    incomingRequest = false,
                    requestDetails = null
                )

                is HomeScreenState.RequestDeclined -> HomeScreenState.Listening(
                    dailyEarnings = _dailyEarnings.value,
                    incomingRequest = false,
                    requestDetails = null
                )

                else -> {
                    updateState(
                        HomeScreenState.Error(
                            Problem(
                                type = "ResetToListeningError",
                                title = "Reset to Listening Error",
                                detail = "Cannot reset to listening state from $current.",
                                status = 403
                            )
                        )
                    )
                }
            }
        }
    }


    fun fetchDailyEarnings(courierId: String, token: String) {
        viewModelScope.launch {
            val result = homeService.getDailyEarnings(courierId, token)
            if (result is Result.Error) {
                updateState(HomeScreenState.Error(result.problem))
            } else if (result is Result.Success) {
                val earnings = result.data
                _dailyEarnings.value = earnings.toString()
            }
        }
    }


    fun startListening() {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            homeService.startListening(
                token = token,
                onMessage = { message ->
                    val request = parseDynamicMessage(message)
                    Log.v("HomeViewModel", "Received message: $message")
                    when (request) {
                        is CourierRequestDetails -> {
                            Log.v("HomeViewModel", "Received delivery request: $request")
                            _stateFlow.update { current ->
                                when (current) {
                                    is HomeScreenState.Listening -> {
                                        HomeScreenState.Listening(
                                            incomingRequest = true,
                                            requestDetails = request,
                                            dailyEarnings = current.dailyEarnings
                                        )
                                    }

                                    else -> {
                                        updateState(
                                            HomeScreenState.Error(
                                                Problem(
                                                    type = "ListeningError",
                                                    title = "Listening Error",
                                                    detail = "Cannot receive requests in the $current state.",
                                                    status = 403
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        is DeliveryUpdate -> {
                            Log.v("HomeViewModel", "Received delivery update: $request")
                            _stateFlow.update { current ->
                                when (current) {
                                    is HomeScreenState.CancellingDropOff -> {
                                        current.copy(
                                            isOrderReassigned = request.hasBeenAccepted,
                                            isOrderPickedUp = request.hasBeenPickedUp,
                                            pickupCode = request.pinCode,
                                        )
                                    }

                                    else -> {
                                        updateState(
                                            HomeScreenState.Error(
                                                Problem(
                                                    type = "ListeningError",
                                                    title = "Listening Error",
                                                    detail = "Cannot receive updates in the $current state.",
                                                    status = 403
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        is ResultMessage ->{
                            _stateFlow.update {
                                when (request.type) {
                                    "SUCCESS" -> {
                                        when (request.subType) {
                                            "ACCEPT" -> {
                                                when (it) {
                                                    is HomeScreenState.Listening -> {
                                                        _currentRequest.value = it.requestDetails
                                                        HomeScreenState.RequestAccepted(
                                                            message = request.message
                                                        )
                                                    }
                                                    is HomeScreenState.RequestAccepted -> it
                                                    else -> HomeScreenState.Error(
                                                        Problem(
                                                            type = "AcceptRequestError",
                                                            title = "Accept Request Error",
                                                            detail = "Cannot accept request in the $it state.",
                                                            status = 403
                                                        )
                                                    )
                                                }
                                            }
                                            "DECLINE" -> {
                                                when (it) {
                                                    is HomeScreenState.Listening -> HomeScreenState.RequestDeclined(
                                                        message = request.message
                                                    )
                                                    is HomeScreenState.RequestDeclined -> it
                                                    else -> HomeScreenState.Error(
                                                        Problem(
                                                            type = "DeclineRequestError",
                                                            title = "Decline Request Error",
                                                            detail = "Cannot decline request in the $it state.",
                                                            status = 403
                                                        )
                                                    )
                                                }
                                            }
                                            else -> {
                                                Log.w("HomeViewModel", "Unhandled subType: ${request.subType}")
                                                HomeScreenState.Error(
                                                    Problem(
                                                        type = "UnhandledSubType",
                                                        title = "Unhandled SubType",
                                                        detail = "Received an unhandled subType: ${request.subType}",
                                                        status = 400
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    "ERROR" -> {
                                        Log.e("HomeViewModel", "Received error message: ${request.message}")
                                        HomeScreenState.Error(
                                            Problem(
                                                type = request.subType ?: "UnknownError",
                                                title = request.message,
                                                detail = request.detail ?: "An error occurred",
                                                status = 500
                                            )
                                        )
                                    }
                                    else -> {
                                        Log.w("HomeViewModel", "Unhandled type: ${request.type}")
                                        HomeScreenState.Error(
                                            Problem(
                                                type = "UnhandledType",
                                                title = "Unhandled Type",
                                                detail = "Received an unhandled type: ${request.type}",
                                                status = 400
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            Log.w("HomeViewModel", "Unknown message type: $request")
                            updateState(
                                HomeScreenState.Error(
                                    Problem(
                                        type = "UnknownMessageType",
                                        title = "Unknown Message Type",
                                        detail = "Received an unknown message type: $request",
                                        status = 400
                                    )
                                )
                            )
                        }
                    }
                },
                onFailure = {
                    _stateFlow.update {
                        when (it) {
                            is HomeScreenState.Listening -> resetToIdleState()
                            else -> {
                                updateState(
                                    HomeScreenState.Error(
                                        Problem(
                                            type = "ListeningError",
                                            title = "Listening Error",
                                            detail = "Failed to start listening: ${it}",
                                            status = 500
                                        )
                                    )
                                )
                            }
                        }
                    }
                }
            )
            _stateFlow.update { current ->
                when (current) {
                    is HomeScreenState.Idle -> HomeScreenState.Listening(
                        dailyEarnings = current.dailyEarnings,
                        incomingRequest = false,
                        requestDetails = null
                    )

                    else -> {
                        updateState(
                            HomeScreenState.Error(
                                Problem(
                                    type = "ListeningError",
                                    title = "Listening Error",
                                    detail = "Cannot start listening in the $current state.",
                                    status = 403
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun stopListening() {
        viewModelScope.launch {
            homeService.stopListening()
            _stateFlow.update { current ->
                when (current) {
                    is HomeScreenState.Listening -> HomeScreenState.Idle(
                        dailyEarnings = current.dailyEarnings,
                    )

                    is HomeScreenState.Delivered -> HomeScreenState.Idle(
                        dailyEarnings = current.dailyEarnings,
                    )

                    is HomeScreenState.CancellingPickup, is HomeScreenState.CancellingDropOff -> HomeScreenState.Idle(
                        dailyEarnings = _dailyEarnings.value,
                    )

                    else -> {
                        updateState(
                            HomeScreenState.Error(
                                Problem(
                                    type = "StopListeningError",
                                    title = "Stop Listening Error",
                                    detail = "You're not currently listening for requests.",
                                    status = 403
                                )
                            )
                        )
                    }
                }
            }
            Log.v("HomeViewModel", "Listening stopped successfully")
        }
    }

    fun acceptRequest(
        requestId: String,
    ) {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result = homeService.acceptRequest(requestId, token)
            if (!result) {
                updateState(
                    HomeScreenState.Error(
                        Problem(
                            type = "RequestError",
                            title = "Request Error",
                            detail = "Failed to accept request.",
                            status = 500
                        )
                    )
                )
            }

        }
    }

    fun headToPickUp(
        context: Context,
        pickupLat: Double,
        pickupLon: Double,
    ){
        Log.v("HomeViewModel", "Request accepted successfully")
        _stateFlow.update { current ->
            when (current) {
                is HomeScreenState.RequestAccepted -> {
                    launchNavigationAppChooser(context, pickupLat, pickupLon)
                    HomeScreenState.HeadingToPickUp()
                }

                else -> {
                    updateState(
                        HomeScreenState.Error(
                            Problem(
                                type = "AcceptRequestError",
                                title = "Accept Request Error",
                                detail = "Cannot accept request in the $current state.",
                                status = 403
                            )
                        )
                    )
                }
            }
        }
        Log.v(
            "HomeViewModel",
            "Request accepted successfully, navigating to pickup location, current state: ${_stateFlow.value}"
        )
    }


    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            val result = homeService.declineRequest(requestId)
            if (!result) {
                updateState(
                    HomeScreenState.Error(
                        Problem(
                            type = "RequestError",
                            title = "Request Error",
                            detail = "Failed to decline request.",
                            status = 500
                        )
                    )
                )
            }
        }
    }

    fun validatePickup(requestId: String, courierId: String, context: Context) {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val currLocation = locationServices.getCurrentLocation()
            if (currLocation is Result.Error) {
                updateState(HomeScreenState.Error(currLocation.problem))
                Log.v(
                    "HomeViewModel",
                    "Failed to get current location: ${currLocation.problem.detail}"
                )
            } else if (currLocation is Result.Success) {
                val updateLocation = locationServices.sendLocationToBackend(
                    lat = currLocation.data.latitude,
                    lon = currLocation.data.longitude,
                    courierId = courierId,
                    authToken = token
                )
                if (updateLocation is Result.Error) {
                    updateState(HomeScreenState.Error(updateLocation.problem))
                    Log.v(
                        "HomeViewModel",
                        "Failed to update location: ${updateLocation.problem.detail}"
                    )
                } else if (updateLocation is Result.Success) {
                    val result = homeService.validatePickup(requestId, courierId, token)
                    if (result is Result.Error) {
                        updateState(HomeScreenState.Error(result.problem))
                    } else {
                        Log.v("HomeViewModel", "Pickup validated successfully")
                        _stateFlow.update { current ->
                            when (current) {
                                is HomeScreenState.HeadingToPickUp -> {
                                    current.copy(isPickUpSpotValid = true)
                                }

                                else -> {
                                    updateState(
                                        HomeScreenState.Error(
                                            Problem(
                                                type = "ValidatePickupError",
                                                title = "Validate Pickup Error",
                                                detail = "Cannot validate pickup in the $current state.",
                                                status = 403
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    fun validatePickUpPin(
        requestId: String,
        courierId: String,
        pickUpPin: String,
        deliveryKind: String,
    ) {
        Log.d(
            "HomeViewModel",
            "pickupOrder() called with requestId: $requestId, courierId: $courierId"
        )
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result =
                homeService.pickupOrder(requestId, courierId, pickUpPin, deliveryKind, token)
            if (result is Result.Error) {
                updateState(HomeScreenState.Error(result.problem))
            } else {
                Log.v("HomeViewModel", "Order picked up successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToPickUp -> {
                            HomeScreenState.PickedUp()
                        }

                        else -> {
                            updateState(
                                HomeScreenState.Error(
                                    Problem(
                                        type = "PickupOrderError",
                                        title = "Pickup Order Error",
                                        detail = "Cannot pick up order in the current state.",
                                        status = 403
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun startNavigationToDropOff(
        context: Context,
    ) {
        viewModelScope.launch {
            _stateFlow.update { current ->
                when (current) {
                    is HomeScreenState.PickedUp -> {
                        launchNavigationAppChooser(
                            context,
                            currentRequest.value!!.dropoffLatitude,
                            currentRequest.value!!.dropoffLongitude
                        )
                        HomeScreenState.HeadingToDropOff()
                    }

                    else -> {
                        updateState(
                            HomeScreenState.Error(
                                Problem(
                                    type = "PickupOrderError",
                                    title = "Pickup Order Error",
                                    detail = "Cannot pick up order in the current state.",
                                    status = 403
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun validateDropOff(requestId: String, courierId: String) {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val currLocation = locationServices.getCurrentLocation()
            if (currLocation is Result.Error) {
                updateState(HomeScreenState.Error(currLocation.problem))
                Log.v(
                    "HomeViewModel",
                    "Failed to get current location: ${currLocation.problem.detail}"
                )
            } else if (currLocation is Result.Success) {
                val updateLocation = locationServices.sendLocationToBackend(
                    lat = currLocation.data.latitude,
                    lon = currLocation.data.longitude,
                    courierId = courierId,
                    authToken = token
                )
                if (updateLocation is Result.Error) {
                    updateState(HomeScreenState.Error(updateLocation.problem))
                    Log.v(
                        "HomeViewModel",
                        "Failed to update location: ${updateLocation.problem.detail}"
                    )
                } else if (updateLocation is Result.Success) {
                    val result = homeService.validateDropOff(requestId, courierId, token)
                    if (result is Result.Error) {
                        updateState(HomeScreenState.Error(result.problem))
                    } else {
                        Log.v("HomeViewModel", "Drop-off validated successfully")
                        _stateFlow.update { current ->
                            when (current) {
                                is HomeScreenState.HeadingToDropOff -> current.copy(
                                    isDropOffSpotValid = true
                                )

                                else -> {
                                    updateState(
                                        HomeScreenState.Error(
                                            Problem(
                                                type = "ValidateDropOffError",
                                                title = "Validate Drop Off Error",
                                                detail = "Cannot validate drop-off in the $current state.",
                                                status = 403
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun validateDropOffPin(
        requestId: String,
        courierId: String,
        dropOffPin: String,
        deliveryEarnings: Double
    ) {
        Log.d(
            "HomeViewModel",
            "deliverOrder() called with requestId: $requestId, courierId: $courierId"
        )
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result =
                homeService.deliverOrder(requestId, courierId, dropOffPin, deliveryEarnings, token)
            if (result is Result.Error) {
                updateState(HomeScreenState.Error(result.problem))
            } else {
                fetchDailyEarnings(courierId, token)
                Log.v("HomeViewModel", "Order delivered successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToDropOff -> {
                            _currentRequest.value = null // Clear the current request after delivery
                            HomeScreenState.Delivered(
                                dailyEarnings = _dailyEarnings.value,
                            )
                        }

                        else -> {
                            updateState(
                                HomeScreenState.Error(
                                    Problem(
                                        type = "DeliverOrderError",
                                        title = "Deliver Order Error",
                                        detail = "Cannot deliver order in the $current state.",
                                        status = 403
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun tryCancelDelivery() {
        viewModelScope.launch {
            _stateFlow.update { current ->
                when (current) {
                    is HomeScreenState.HeadingToPickUp -> {
                        _previousState.value = current
                        HomeScreenState.CancellingOrder(
                            deliveryStatus = current.deliveryStatus
                        )
                    }

                    is HomeScreenState.HeadingToDropOff -> {
                        _previousState.value = current
                        HomeScreenState.CancellingOrder(
                            deliveryStatus = current.deliveryStatus
                        )
                    }

                    else -> {
                        updateState(
                            HomeScreenState.Error(
                                Problem(
                                    type = "CancelDeliveryError",
                                    title = "Cancel Delivery Error",
                                    detail = "Cannot try and cancel delivery in the ${current::class.simpleName} state.",
                                    status = 403
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    fun cancelDelivery(
        courierId: String,
        requestId: String,
        deliveryStatus: String,
    ) {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val currLocation = locationServices.getCurrentLocation()
            if (currLocation is Result.Error) {
                updateState(HomeScreenState.Error(currLocation.problem))
                Log.v(
                    "HomeViewModel",
                    "Failed to get current location: ${currLocation.problem.detail}"
                )
            } else if (currLocation is Result.Success) {
                val pickUpLocation = when (deliveryStatus) {
                    "HEADING_TO_DROPOFF" -> LocationDTO(
                        currLocation.data.latitude,
                        currLocation.data.longitude
                    )

                    else -> null
                }
                val result = homeService.cancelDelivery(
                    courierId,
                    requestId,
                    deliveryStatus,
                    pickUpLocation,
                    token
                )
                if (result is Result.Error) {
                    updateState(HomeScreenState.Error(result.problem))
                    Log.v("HomeViewModel", "Failed to cancel delivery: ${result.problem.detail}")
                } else {
                    Log.v("HomeViewModel", "Delivery cancelled successfully")
                    when (deliveryStatus) {
                        "HEADING_TO_PICKUP" -> {
                            _stateFlow.update { current ->
                                when (current) {
                                    is HomeScreenState.CancellingOrder -> {
                                        HomeScreenState.CancellingPickup()
                                    }
//TODO(check the use case where the state is Idle upon cancelling a delivery)
//                                    is HomeScreenState.Idle -> {
//                                        Log.v(
//                                            "HomeViewModel",
//                                            "Already in idle state, no action needed"
//                                        )
//                                        HomeScreenState.CancellingPickup(
//                                            courierId = courierId,
//                                            requestId = requestId,
//                                        )
//                                    }

                                    is HomeScreenState.Error -> {
                                        Log.v(
                                            "HomeViewModel",
                                            "Error state encountered: ${current.problem.detail}"
                                        )
                                        updateState(
                                            HomeScreenState.Error(
                                                problem = current.problem
                                            )
                                        )
                                    }

                                    else -> {
                                        resetToIdleState()
                                    }
                                }
                            }
                        }

                        "HEADING_TO_DROPOFF" -> {
                            _stateFlow.update { current ->
                                when (current) {
                                    is HomeScreenState.CancellingOrder -> {
                                        HomeScreenState.CancellingDropOff(
                                            isOrderReassigned = false,
                                            pickUpLocation = pickUpLocation,
                                        )
                                    }

                                    is HomeScreenState.Idle -> {
                                        Log.v(
                                            "HomeViewModel",
                                            "Already in idle state, no action needed"
                                        )
                                        HomeScreenState.CancellingDropOff(
                                            isOrderReassigned = false,
                                            pickUpLocation = pickUpLocation,
                                        )
                                    }

                                    is HomeScreenState.Error -> {
                                        Log.v(
                                            "HomeViewModel",
                                            "Error state encountered: ${current.problem.detail}"
                                        )
                                        updateState(
                                            HomeScreenState.Error(
                                                problem = current.problem
                                            )
                                        )
                                    }

                                    else -> {
                                        Log.v(
                                            "HomeViewModel",
                                            "Cannot cancel delivery in the current state: $current"
                                        )
                                        updateState(
                                            HomeScreenState.Error(
                                                Problem(
                                                    type = "CancelDeliveryError",
                                                    title = "Cancel Delivery Error",
                                                    detail = "Cannot cancel delivery in the $current state.",
                                                    status = 403
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            Log.v("HomeViewModel", "Unknown delivery status: $deliveryStatus")
                        }
                    }
                }
            }
        }
    }

    fun toggleOrderInfoVisibility(){
        _stateFlow.update { current ->
            when (current) {
                is HomeScreenState.HeadingToPickUp -> {
                    current.copy(isOrderInfoVisible = !current.isOrderInfoVisible)
                }

                is HomeScreenState.HeadingToDropOff -> {
                    current.copy(isOrderInfoVisible = !current.isOrderInfoVisible)
                }
                else -> {
                    updateState(
                        HomeScreenState.Error(
                            Problem(
                                type = "ToggleOrderInfoError",
                                title = "Toggle Order Info Error",
                                detail = "Cannot toggle order info visibility in the $current state.",
                                status = 403
                            )
                        )
                    )
                }
            }
        }
    }

    fun resetToIdleState(): HomeScreenState {
        stopListening()
        return HomeScreenState.Idle(
            dailyEarnings = _dailyEarnings.value
        )
    }
}

fun parseDynamicMessage(message: String): Any {
    val mapper = jacksonObjectMapper()
    val root: JsonNode = mapper.readTree(message)
    val type = root.get("type")?.asText() ?: throw IllegalArgumentException("Missing type field")

    return when (type) {
        "DELIVERY_REQUEST" -> mapper.treeToValue(root, CourierRequestDetails::class.java)
        "DELIVERY_UPDATE" -> mapper.treeToValue(root, DeliveryUpdate::class.java)
        "SUCCESS", "ERROR" -> mapper.treeToValue(root, ResultMessage::class.java)
        else -> throw IllegalArgumentException("Unknown message type: $type")
    }
}

fun launchNavigationAppChooser(
    context: Context,
    Lat: Double,
    Lng: Double,
) {
    // Google Maps URI with pickup as waypoint and drop-off as destination
    val googleMapsUri = ("https://www.google.com/maps/dir/?api=1" +
            "&origin=My+Location" +
            "&destination=$Lat,$Lng" +
            "&travelmode=driving" +
            "&dir_action=navigate").toUri()
    val googleMapsIntent = Intent(Intent.ACTION_VIEW, googleMapsUri).apply {
        setPackage("com.google.android.apps.maps")
    }

    // Waze URI to pickup location only
    val wazeUri = "https://waze.com/ul?ll=$Lat,$Lng&navigate=yes".toUri()
    val wazeIntent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
        setPackage("com.waze")
    }

    // Create chooser intent
    val chooserIntent =
        Intent.createChooser(googleMapsIntent, "Choose an app for navigation").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(wazeIntent))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

    context.startActivity(chooserIntent)
}


