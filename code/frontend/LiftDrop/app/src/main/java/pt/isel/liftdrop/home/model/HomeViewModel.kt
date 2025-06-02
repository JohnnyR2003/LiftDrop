package pt.isel.liftdrop.home.model

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pt.isel.liftdrop.home.ui.HomeScreen
import pt.isel.liftdrop.home.ui.HomeScreenState
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.model.PreferencesRepository


class HomeViewModel(
    private val homeService: HomeService,
    private val loginService: LoginService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    /*private val _state = MutableStateFlow(
        HomeScreenState(
            dailyEarnings = "0.00",
            isUserLoggedIn = false,
            isListening = false,
            incomingRequest = false,
            requestDetails = null,
        )
    )
    val homeScreenState: StateFlow<HomeScreenState> = _state.asStateFlow()*/

    val stateFlow: Flow<HomeScreenState>
        get() = _stateFlow.asStateFlow()

    private val _stateFlow: MutableStateFlow<HomeScreenState> =
        MutableStateFlow(HomeScreenState.Idle())

    val dailyEarnings: Flow<String>
        get() = _dailyEarnings.asStateFlow()

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
            val fgLocationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                permissions[Manifest.permission.FOREGROUND_SERVICE_LOCATION] ?: false
            } else true

            if ((fineGranted || coarseGranted) && fgLocationGranted) {
                val userInfo = preferences.getUserInfo()
                if (userInfo != null) {
                    startLocationService(userInfo.bearer, userInfo.courierId.toString())
                    _serviceStarted.value = true
                }
            }
        }
    }


    fun logout() {
        if (_stateFlow.value !is HomeScreenState.Idle || _stateFlow.value is HomeScreenState.Listening) {
            _stateFlow.value =
                HomeScreenState.Error(
                    Exception("Cannot log out while the view model is not in the idle state or while listening for requests.")
                )
            //TODO: FIND A WAY TO WARN THE USER THAT THEY ARE LOGGING OUT
        }
        viewModelScope.launch(Dispatchers.IO) {
            val userInfo = preferences.getUserInfo()
            if (userInfo != null) {
                preferences.clearUserInfo(userInfo)
                val result = runCatching {
                    loginService.logout(
                        userInfo.bearer,
                        userInfo.courierId.toString()
                    )
                }
                if (result.isFailure) {
                    _stateFlow.value =
                        HomeScreenState.Error(
                            result.exceptionOrNull() ?: Exception("Unknown error")
                        )
                } else {
                    // Log.v("Home", "logged out done")
                    preferences.clearUserInfo(userInfo)
                    _stateFlow.value = HomeScreenState.Logout(true)
                }
            }
            else {
                _stateFlow.value = HomeScreenState.Error(Exception("User not logged in"))
            }
        }
    }

    fun resetToIdle() {
        check(_stateFlow.value !is HomeScreenState.Idle) {
            "The view model is already in the idle state."
        }
        _stateFlow.value = HomeScreenState.Idle()
    }


    fun fetchDailyEarnings(courierId: String, token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { homeService.getDailyEarnings(courierId, token) }

            if (result.isFailure) {
                _stateFlow.value = HomeScreenState.Error(
                    result.exceptionOrNull() ?: Exception("Unknown error")
                )
            } else {
                val earnings = result.getOrThrow()
                _dailyEarnings.value = earnings.toString()
                }
            }
        }


    fun startListening() {
        viewModelScope.launch {
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result =
                runCatching { homeService.startListening(
                    token = token,
                    onMessage = { message ->
                        val request = parseRequestDetails(message)
                        _stateFlow.update { current ->
                            when (current) {
                                is HomeScreenState.Listening -> HomeScreenState.Listening(
                                    incomingRequest = true,
                                    requestDetails = request,
                                    dailyEarnings = current.dailyEarnings
                                )
                                is HomeScreenState.Idle -> throw IllegalStateException("The view model cannot receive requests while idle, please start listening first.")
                                is HomeScreenState.PickingUp -> throw IllegalStateException("The view model cannot receive requests while picking up an order.")
                                is HomeScreenState.Delivering -> throw IllegalStateException("The view model cannot receive requests while delivering an order.")
                                is HomeScreenState.Delivered -> throw  IllegalStateException("The view model cannot receive requests after delivering an order, please start listening again.")
                                is HomeScreenState.Logout -> throw IllegalStateException("The view model cannot receive requests after logging out, please log in again and then start listening.")
                                is HomeScreenState.Error -> throw IllegalStateException(message)
                            }
                        }
                    },
                    onFailure = {
                        throw IllegalStateException("Failed to start listening: $it")
                    }
                )
                }
            if (result.isFailure) {
                _stateFlow.value =
                    HomeScreenState.Error(
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
            }
            else when (val current = _stateFlow.value) {
                is HomeScreenState.Idle -> _stateFlow.value = HomeScreenState.Listening(
                    dailyEarnings = current.dailyEarnings,
                    incomingRequest = false,
                    requestDetails = null
                )
                is HomeScreenState.Listening -> Log.v("HomeViewModel", "Already listening")
                is HomeScreenState.PickingUp -> throw IllegalStateException("Cannot start listening while picking up an order.")
                is HomeScreenState.Delivering -> throw IllegalStateException("Cannot start listening while delivering an order.")
                is HomeScreenState.Delivered -> throw IllegalStateException("Cannot start listening after delivering an order, please start listening again.")
                is HomeScreenState.Logout -> throw IllegalStateException("Cannot start listening after logging out, please log in again and then start listening.")
                is HomeScreenState.Error -> throw IllegalStateException("Cannot start listening in error state: ${current.message}")
            }
        }
    }

    fun stopListening() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching { homeService.stopListening() }
            if (result.isFailure) {
                _stateFlow.value =
                    HomeScreenState.Error(
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
            } else {
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.Listening -> HomeScreenState.Idle(
                            dailyEarnings = current.dailyEarnings,
                        )

                        is HomeScreenState.Delivered -> HomeScreenState.Idle(
                            dailyEarnings = current.dailyEarnings,
                        )
                        else -> current
                    }
                }
                Log.v("HomeViewModel", "Listening stopped successfully")
            }
        }
    }

    fun acceptRequest(
        requestId: String,
        context: Context,
        pickupLat: Double,
        pickupLon: Double,
    ) {
        viewModelScope.launch {
            try {
                val token = preferences.getUserInfo()?.bearer
                    ?: throw IllegalStateException("User not logged in, please log in first.")
                val result = runCatching { homeService.acceptRequest(requestId, token) }
                if (result.isFailure) {
                    _stateFlow.value =
                        HomeScreenState.Error(
                            result.exceptionOrNull() ?: Exception("Unknown error")
                        )
                } else {
                    Log.v("HomeViewModel", "Request accepted successfully")
                    _stateFlow.update { current ->
                        when (current) {
                            is HomeScreenState.Listening -> {
                                launchNavigationAppChooser(context, pickupLat, pickupLon)
                                HomeScreenState.PickingUp(
                                    dropoffCoordinates = Pair(
                                        current.requestDetails!!.dropoffLatitude,
                                        current.requestDetails.dropoffLongitude
                                    ),
                                    dailyEarnings = current.dailyEarnings,
                                    pickedUp = false,
                                    requestId = current.requestDetails.requestId,
                                    courierId = current.requestDetails.courierId,
                                )
                            }
                            is HomeScreenState.Idle -> throw IllegalStateException("Cannot accept request in idle state, please start listening first.")
                            is HomeScreenState.PickingUp -> throw IllegalStateException("Cannot accept request while picking up an order.")
                            is HomeScreenState.Delivering -> throw IllegalStateException("Cannot accept request while delivering an order.")
                            is HomeScreenState.Delivered -> throw IllegalStateException("Cannot accept request after delivering an order, please start listening again.")
                            is HomeScreenState.Logout -> throw IllegalStateException("Cannot accept request after logging out, please log in again and then start listening.")
                            is HomeScreenState.Error -> throw IllegalStateException("Cannot accept request in error state: ${current.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Accept error: ${e.message}")
                _stateFlow.value = HomeScreenState.Error(e)
            }
        }
    }


    fun declineRequest(requestId: String) {
        viewModelScope.launch {
            val result = runCatching { homeService.declineRequest(requestId) }
            if (result.isFailure) {
                _stateFlow.value =
                    HomeScreenState.Error(
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
            } else {
                Log.v("HomeViewModel", "Request declined successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.Listening -> current.copy(
                            incomingRequest = false,
                            requestDetails = null
                        )
                        is HomeScreenState.PickingUp -> throw IllegalStateException("Cannot decline request while picking up an order.")
                        is HomeScreenState.Delivering -> throw IllegalStateException("Cannot decline request while delivering an order.")
                        is HomeScreenState.Delivered -> throw IllegalStateException("Cannot decline request after delivering an order, please start listening again.")
                        is HomeScreenState.Idle -> throw IllegalStateException("Cannot decline request in idle state, please start listening first.")
                        is HomeScreenState.Logout -> throw IllegalStateException("Cannot decline request after logging out, please log in again and then start listening.")
                        is HomeScreenState.Error -> throw IllegalStateException("Cannot decline request in error state: ${current.message}")
                    }
                }
            }
        }
    }

    fun pickupOrder(requestId: String, courierId: String, context: Context){
        Log.d("HomeViewModel", "pickupOrder() called with requestId: $requestId, courierId: $courierId")
        viewModelScope.launch(Dispatchers.IO){
            try{
                val token = preferences.getUserInfo()?.bearer
                    ?: throw IllegalStateException("User not logged in, please log in first.")
                val result = runCatching {
                    homeService.pickupOrder(requestId, courierId, token)
                }
                if (result.isFailure) {
                    _stateFlow.value = HomeScreenState.Error(
                        result.exceptionOrNull() ?: Exception("Unknown error")
                    )
                } else {
                    Log.v("HomeViewModel", "Order picked up successfully")
                    _stateFlow.update { current ->
                        when (current) {
                            is HomeScreenState.PickingUp -> {
                                launchNavigationAppChooser(context, current.dropoffCoordinates!!.first, current.dropoffCoordinates.second)
                                HomeScreenState.Delivering(
                                dailyEarnings = current.dailyEarnings,
                                delivered = false,
                                requestId = current.requestId,
                                courierId = current.courierId
                                )
                            }
                            is HomeScreenState.Listening -> throw IllegalStateException("Cannot pick up order while listening for new requests.")
                            is HomeScreenState.Idle -> throw IllegalStateException("Cannot pick up order in idle state, please start listening first.")
                            is HomeScreenState.Delivering -> throw IllegalStateException("Cannot pick up order while delivering an order.")
                            is HomeScreenState.Delivered -> throw IllegalStateException("Cannot pick up order after delivering an order, please start listening again.")
                            is HomeScreenState.Logout -> throw IllegalStateException("Cannot pick up order after logging out, please log in again and then start listening.")
                            is HomeScreenState.Error -> throw IllegalStateException("Cannot pick up order in error state: ${current.message}")
                        }
                    }
                }
            }catch (e: Exception) {
                Log.e("HomeViewModel", "Pickup error: ${e.message}")
                _stateFlow.value = HomeScreenState.Error(e)
            }
        }
    }

    fun deliverOrder(requestId: String, courierId: String){
        viewModelScope.launch(Dispatchers.IO){
            val token = preferences.getUserInfo()?.bearer
                ?: throw IllegalStateException("User not logged in, please log in first.")
            val result = runCatching {
                homeService.deliverOrder(requestId, courierId, token)
            }
            if (result.isFailure) {
                _stateFlow.value = HomeScreenState.Error(
                    result.exceptionOrNull() ?: Exception("Unknown error")
                )
            } else {
                val earnings = runCatching { fetchDailyEarnings(courierId, token) }
                if (earnings.isFailure) {
                    _stateFlow.value = HomeScreenState.Error(
                        earnings.exceptionOrNull() ?: Exception("Unknown error while fetching daily earnings")
                    )
                }
                Log.v("HomeViewModel", "Order delivered successfully")
                _stateFlow.update { current ->
                    when (current) {
                        is HomeScreenState.Delivering -> HomeScreenState.Listening(
                            incomingRequest = false,
                            requestDetails = null,
                            dailyEarnings = current.dailyEarnings
                        )
                            /*TODO: implement Delivered screen    HomeScreenState.Delivered(
                            dailyEarnings = current.dailyEarnings,
                        )*/
                        is HomeScreenState.Listening -> throw IllegalStateException("Cannot deliver order while listening for new requests.")
                        is HomeScreenState.Idle -> throw IllegalStateException("Cannot deliver order in idle state, please start listening first.")
                        is HomeScreenState.PickingUp -> throw IllegalStateException("Cannot deliver order while picking up an order.")
                        is HomeScreenState.Delivered -> throw IllegalStateException("Cannot deliver order after delivering an order, please start listening again.")
                        is HomeScreenState.Logout -> throw IllegalStateException("Cannot deliver order after logging out, please log in again and then start listening.")
                        is HomeScreenState.Error -> throw IllegalStateException("Cannot deliver order in error state: ${current.message}")
                    }
                }
            }
        }
    }
}

private fun parseRequestDetails(message: String): CourierRequestDetails {
    val mapper = jacksonObjectMapper()
    val details = mapper.readValue(message, CourierRequestDetails::class.java)
    return CourierRequestDetails(
        requestId = details.requestId,
        courierId = details.courierId,
        pickupLatitude = details.pickupLatitude,
        pickupLongitude = details.pickupLongitude,
        dropoffLatitude = details.dropoffLatitude,
        dropoffLongitude = details.dropoffLongitude,
        pickupAddress = details.pickupAddress,
        dropoffAddress = details.dropoffAddress,
        price = details.price
    )
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
    val chooserIntent = Intent.createChooser(googleMapsIntent, "Choose an app for navigation").apply {
        putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(wazeIntent))
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    context.startActivity(chooserIntent)
}


