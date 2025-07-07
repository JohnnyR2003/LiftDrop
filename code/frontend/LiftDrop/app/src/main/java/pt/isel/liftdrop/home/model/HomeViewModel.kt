package pt.isel.liftdrop.home.model

import android.Manifest
import android.annotation.SuppressLint
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
import pt.isel.liftdrop.home.model.dto.LocationDTO
import pt.isel.liftdrop.home.model.websocket.DeliveryUpdate
import pt.isel.liftdrop.home.model.websocket.HomeMessage
import pt.isel.liftdrop.home.model.websocket.IncomingRequestDetails
import pt.isel.liftdrop.home.model.websocket.MessageType
import pt.isel.liftdrop.home.model.websocket.ResultMessage
import pt.isel.liftdrop.home.model.websocket.ResultSubType
import pt.isel.liftdrop.home.model.websocket.ResultType
import pt.isel.liftdrop.home.ui.screens.HomeScreenState
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.preferences.PreferencesRepository
import pt.isel.liftdrop.services.location.LocationTrackingService
import pt.isel.liftdrop.services.http.Problem
import pt.isel.liftdrop.services.http.Result
import kotlin.to

class HomeViewModel(
    private val homeService: HomeService,
    private val loginService: LoginService,
    private val locationServices: LocationTrackingService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    private val _currentRequest: MutableStateFlow<IncomingRequestDetails?> =
        MutableStateFlow(null)

    private val _stateFlow: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Idle("0.00"))

    private val _previousState: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Idle("0.00"))

    val currentRequest: StateFlow<IncomingRequestDetails?>
        get() = _currentRequest.asStateFlow()
    val stateFlow: StateFlow<HomeScreenState> = _stateFlow.asStateFlow()
    val previousState: StateFlow<HomeScreenState> = _previousState.asStateFlow()

    val dailyEarnings: Flow<String>
        get() = _dailyEarnings.asStateFlow()

    init {
        // Start tracking previous screen state
        viewModelScope.launch {
            _stateFlow
                .drop(1) // Skip initial Idle state if needed
                .scan(_previousState.value to _stateFlow.value) { acc, new ->
                    acc.second to new
                }
                .collectLatest { (prev, _) ->
                    _previousState.value = prev
                }
        }
    }

    val _dailyEarnings: MutableStateFlow<String> = MutableStateFlow("0.00")

    val _serviceStarted: MutableStateFlow<Boolean> = MutableStateFlow(false)
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

    fun transitionTo(state: HomeScreenState) {
        _stateFlow.update { state }
    }

    fun raiseError(problem: Problem) {
        _stateFlow.update { HomeScreenState.Error(problem = problem) }
    }

    fun dismissError() {
        _stateFlow.update { previousState.value }
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
                    raiseError(result.problem)
                } else {
                    preferences.clearUserInfo(userInfo)
                    transitionTo(HomeScreenState.Logout(true))
                }
            } else {
                raiseError(
                    Problem(
                        type = "LogoutError",
                        title = "Logout Error",
                        detail = "You are not logged in.",
                        status = 403
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
                is HomeScreenState.Delivered -> {
                    _currentRequest.update { null }
                    HomeScreenState.Listening(
                        dailyEarnings = current.dailyEarnings,
                        incomingRequest = false,
                        requestDetails = null
                    )
                }

                is HomeScreenState.RequestDeclined -> HomeScreenState.Listening(
                    dailyEarnings = _dailyEarnings.value,
                    incomingRequest = false,
                    requestDetails = null
                )

                else -> {
                    HomeScreenState.Error(
                        Problem(
                            type = "ResetToListeningError",
                            title = "Reset to Listening Error",
                            detail = "Cannot reset to listening state from $current.",
                            status = 403
                        )
                    )
                }
            }
        }
    }


    @SuppressLint("DefaultLocale")
    fun fetchDailyEarnings(courierId: String, token: String) {
        viewModelScope.launch {
            val result = homeService.getDailyEarnings(courierId, token)
            if (result is Result.Error) {
                raiseError(result.problem)
            } else if (result is Result.Success) {
                val earnings = result.data
                _dailyEarnings.value = String.format("%.2f", earnings)
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
                    when (request) {
                        is IncomingRequestDetails -> {
                            _stateFlow.update { current ->
                                processIncomingRequest(current, request)
                            }
                        }

                        is DeliveryUpdate -> {
                            _stateFlow.update { current ->
                                processDeliveryUpdate(current, request)
                            }
                        }

                        is ResultMessage -> {
                            _stateFlow.update { current ->
                                processResultMessage(current, request)
                            }
                        }
                    }
                },
                onFailure = {
                    _stateFlow.update { current ->
                        resetToIdleState()
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
                        HomeScreenState.Error(
                            Problem(
                                type = "ListeningError",
                                title = "Listening Error",
                                detail = "Cannot start listening in the $current state.",
                                status = 403
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
                        HomeScreenState.Error(
                            Problem(
                                type = "StopListeningError",
                                title = "Stop Listening Error",
                                detail = "You're not currently listening for requests.",
                                status = 403
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
                HomeScreenState.Error(
                    Problem(
                        type = "RequestError",
                        title = "Request Error",
                        detail = "Failed to accept request.",
                        status = 500
                    )
                )
            }
        }
    }

    fun headToPickUp(
        context: Context,
        pickupLat: Double,
        pickupLon: Double,
    ) {
        Log.v("HomeViewModel", "Request accepted successfully")
        _stateFlow.update { current ->
            when (current) {
                is HomeScreenState.RequestAccepted -> {
                    launchNavigationAppChooser(context, pickupLat, pickupLon)
                    HomeScreenState.HeadingToPickUp()
                }

                else -> {
                    HomeScreenState.Error(
                        Problem(
                            type = "AcceptRequestError",
                            title = "Accept Request Error",
                            detail = "Cannot accept request in the $current state.",
                            status = 403
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
                raiseError(
                    Problem(
                        type = "RequestError",
                        title = "Request Error",
                        detail = "Failed to decline request.",
                        status = 500
                    )
                )
            }
        }
    }

    fun validatePickup(requestId: String, courierId: String, context: Context) {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result = homeService.validatePickup(requestId, courierId, token)
            if (result is Result.Error) {
                raiseError(result.problem)
            } else {
                Log.v("HomeViewModel", "Pickup validated successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToPickUp -> {
                            current.copy(isPickUpSpotValid = true)
                        }

                        else -> {
                            HomeScreenState.Error(
                                Problem(
                                    type = "ValidatePickupError",
                                    title = "Validate Pickup Error",
                                    detail = "Cannot validate pickup in the $current state.",
                                    status = 403
                                )
                            )
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
                raiseError(result.problem)
            } else {
                Log.v("HomeViewModel", "Order picked up successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToPickUp -> {
                            HomeScreenState.PickedUp()
                        }

                        else -> {
                            HomeScreenState.Error(
                                Problem(
                                    type = "PickupOrderError",
                                    title = "Pickup Order Error",
                                    detail = "Cannot pick up order in the current state.",
                                    status = 403
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
                        HomeScreenState.Error(
                            Problem(
                                type = "PickupOrderError",
                                title = "Pickup Order Error",
                                detail = "Cannot pick up order in the current state.",
                                status = 403
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
            val result = homeService.validateDropOff(requestId, courierId, token)
            if (result is Result.Error) {
                raiseError(result.problem)
            } else {
                Log.v("HomeViewModel", "Drop-off validated successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToDropOff -> current.copy(
                            isDropOffSpotValid = true
                        )

                        else -> {
                            HomeScreenState.Error(
                                Problem(
                                    type = "ValidateDropOffError",
                                    title = "Validate Drop Off Error",
                                    detail = "Cannot validate drop-off in the $current state.",
                                    status = 403
                                )
                            )
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
                raiseError(result.problem)
            } else {
                fetchDailyEarnings(courierId, token)
                Log.v("HomeViewModel", "Order delivered successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.HeadingToDropOff -> {
                            HomeScreenState.Delivered(
                                dailyEarnings = _dailyEarnings.value,
                            )
                        }

                        else -> {
                            HomeScreenState.Error(
                                Problem(
                                    type = "DeliverOrderError",
                                    title = "Deliver Order Error",
                                    detail = "Cannot deliver order in the $current state.",
                                    status = 403
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
                        HomeScreenState.CancellingOrder(
                            deliveryStatus = current.deliveryStatus
                        )
                    }

                    is HomeScreenState.HeadingToDropOff -> {
                        HomeScreenState.CancellingOrder(
                            deliveryStatus = current.deliveryStatus
                        )
                    }

                    else -> {
                        HomeScreenState.Error(
                            Problem(
                                type = "CancelDeliveryError",
                                title = "Cancel Delivery Error",
                                detail = "Cannot try and cancel delivery in the ${current::class.simpleName} state.",
                                status = 403
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
                raiseError(currLocation.problem)
                return@launch
            }
            if (currLocation is Result.Success) {
                val status = DeliveryStatus.fromString(deliveryStatus)
                val pickUpLocation =
                    if (status == DeliveryStatus.HEADING_TO_DROPOFF) {
                        LocationDTO(currLocation.data.latitude, currLocation.data.longitude)
                    } else null

                val result = homeService.cancelDelivery(
                    courierId, requestId, deliveryStatus, pickUpLocation, token
                )

                if (result is Result.Error) {
                    raiseError(result.problem)
                    return@launch
                }

                _stateFlow.update { current ->
                    when (status) {
                        DeliveryStatus.HEADING_TO_PICKUP -> when (current) {
                            is HomeScreenState.CancellingOrder -> {
                                _currentRequest.update { null }
                                HomeScreenState.CancellingPickup()
                            }
                            is HomeScreenState.Error -> HomeScreenState.Error(current.problem)
                            else -> resetToIdleState()
                        }
                        DeliveryStatus.HEADING_TO_DROPOFF -> when (current) {
                            is HomeScreenState.CancellingOrder, is HomeScreenState.Idle ->
                                HomeScreenState.CancellingDropOff(
                                    isOrderReassigned = false,
                                    pickUpLocation = pickUpLocation,
                                )
                            //is HomeScreenState.Error -> current
                            else ->
                                HomeScreenState.Error(
                                    Problem(
                                        type = "CancelDeliveryError",
                                        title = "Cancel Delivery Error",
                                        detail = "Cannot cancel delivery in the $current state.",
                                        status = 403
                                    )
                                )
                        }
                    }
                }
            }
        }
    }

    fun toggleOrderInfoVisibility() {
        _stateFlow.update { current ->
            when (current) {
                is HomeScreenState.HeadingToPickUp -> {
                    current.copy(isOrderInfoVisible = !current.isOrderInfoVisible)
                }

                is HomeScreenState.HeadingToDropOff -> {
                    current.copy(isOrderInfoVisible = !current.isOrderInfoVisible)
                }

                else -> {
                    HomeScreenState.Error(
                        Problem(
                            type = "ToggleOrderInfoError",
                            title = "Toggle Order Info Error",
                            detail = "Cannot toggle order info visibility in the $current state.",
                            status = 403
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

    fun parseDynamicMessage(message: String): HomeMessage {
        val mapper = jacksonObjectMapper()
        val root: JsonNode = mapper.readTree(message)
        val typeValue = root.get("type")?.asText() ?: throw IllegalArgumentException("Missing type field")
        val type = MessageType.fromValue(typeValue) ?: throw IllegalArgumentException("Unknown message type: $typeValue")

        return when (type) {
            MessageType.DELIVERY_REQUEST -> mapper.treeToValue(root, IncomingRequestDetails::class.java)
            MessageType.DELIVERY_UPDATE -> mapper.treeToValue(root, DeliveryUpdate::class.java)
            MessageType.SUCCESS, MessageType.ERROR -> mapper.treeToValue(root, ResultMessage::class.java)
        }
    }

    fun processIncomingRequest(
        state: HomeScreenState,
        requestDetails: IncomingRequestDetails
    ): HomeScreenState {
        return when (state) {
            is HomeScreenState.Listening -> {
                HomeScreenState.Listening(
                    incomingRequest = true,
                    requestDetails = requestDetails,
                    dailyEarnings = state.dailyEarnings
                )
            }

            else -> {
                HomeScreenState.Error(
                    Problem(
                        type = "ListeningError",
                        title = "Listening Error",
                        detail = "Cannot receive requests in the $state state.",
                        status = 403
                    )
                )
            }
        }
    }

    fun processResultMessage(state: HomeScreenState, result: ResultMessage): HomeScreenState {
        return when (result.type) {
            ResultType.SUCCESS -> {
                when (result.subType) {
                    ResultSubType.ACCEPT -> {
                        when (state) {
                            is HomeScreenState.Listening -> {
                                _currentRequest.update { state.requestDetails }
                                HomeScreenState.RequestAccepted(
                                    message = result.message
                                )
                            }

                            //is HomeScreenState.RequestAccepted -> state
                            else -> HomeScreenState.Error(
                                Problem(
                                    type = "AcceptRequestError",
                                    title = "Accept Request Error",
                                    detail = "Cannot accept request in the $state state.",
                                    status = 403
                                )
                            )
                        }
                    }

                    ResultSubType.DECLINE -> {
                        when (state) {
                            is HomeScreenState.Listening -> HomeScreenState.RequestDeclined(
                                message = result.message
                            )

                            is HomeScreenState.RequestDeclined -> state
                            else -> HomeScreenState.Error(
                                Problem(
                                    type = "DeclineRequestError",
                                    title = "Decline Request Error",
                                    detail = "Cannot decline request in the $state state.",
                                    status = 403
                                )
                            )
                        }
                    }

                    ResultSubType.UNKNOWN -> {
                        Log.w("HomeViewModel", "Received unknown success message: ${result.message}")
                        HomeScreenState.Error(
                            Problem(
                                type = "UnknownSuccessMessage",
                                title = "Unknown Success Message",
                                detail = "Received an unknown success message: ${result.message}",
                                status = 400
                            )
                        )
                    }
                }
            }

            ResultType.ERROR -> {
                Log.e("HomeViewModel", "Received error message: ${result.message}")
                HomeScreenState.Error(
                    Problem(
                        type = result.subType.name,
                        title = result.message,
                        detail = result.detail ?: "An error occurred",
                        status = 500
                    )
                )
            }
        }
    }

    fun processDeliveryUpdate(
        state: HomeScreenState,
        request: DeliveryUpdate
    ): HomeScreenState {
        return when (state) {
            is HomeScreenState.CancellingDropOff -> {
                state.copy(
                    isOrderReassigned = request.hasBeenAccepted,
                    isOrderPickedUp = request.hasBeenPickedUp,
                    pickupCode = request.pinCode,
                )
            }

            else -> {
                HomeScreenState.Error(
                    Problem(
                        type = "ListeningError",
                        title = "Listening Error",
                        detail = "Cannot receive updates in the $request state.",
                        status = 403
                    )
                )
            }
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
}


