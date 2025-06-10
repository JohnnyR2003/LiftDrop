package pt.isel.liftdrop.home.ui

import DeliveryEarningsCard
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import pt.isel.liftdrop.home.model.HomeViewModel
import pt.isel.liftdrop.location.LocationServices
import androidx.compose.material.icons.filled.ExitToApp
import pt.isel.liftdrop.services.http.Problem
import pt.isel.liftdrop.shared.ui.BottomSlideToConfirm
import pt.isel.liftdrop.shared.ui.SlideToConfirmButton

/*
data class HomeScreenState(
    val dailyEarnings: String,
    val isUserLoggedIn: Boolean,
    val isListening: Boolean,
    val isRequestAccepted: Boolean = false,
    val isPickedUp: Boolean = false,
    val isDelivered: Boolean = false,
    val incomingRequest: Boolean = false,
    val requestDetails: CourierRequestDetails? = null
)*/

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    state: HomeScreenState,
    dailyEarnings: String,
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onCancelDeliveryClick: () -> Unit = {},
) {
    Scaffold(
        topBar = {},
        containerColor = Color.White,
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Map
            LocationServices().LocationAwareMap()

            // Earnings
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 32.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dailyEarnings,
                        fontSize = 30.sp,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF384259)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "€",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFC2F1D5)
                    )
                }
            }

            @Composable
            fun ErrorCard(problem: Problem, onDismiss: () -> Unit) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(0.8f),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = problem.title ?: "Error",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.Red
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = problem.detail ?: "An unknown error occurred.",
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onDismiss) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 120.dp, end = 7.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is HomeScreenState.Idle -> {
                        listOf(
                            Pair(Icons.Default.Menu, onMenuClick),
                            Pair(Icons.Default.Notifications, onNotificationClick),
                            Pair(Icons.Default.ExitToApp, onLogoutClick)
                        ).forEach { (icon, action) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = action,
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color(0xFF384259) // Cor mais clara quando desativado
                                    )
                                }
                            }
                        }
                    }

                    is HomeScreenState.Listening -> {
                        // Show only menu and notifications icons
                        listOf(
                            Pair(Icons.Default.Menu, onMenuClick),
                            Pair(Icons.Default.Notifications, onNotificationClick)
                        ).forEach { (icon, action) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = action,
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color(0xFF384259) // Cor mais clara quando desativado
                                    )
                                }
                            }
                        }
                    }

                    is HomeScreenState.PickingUp, is HomeScreenState.Delivering -> {
                        listOf(
                            Pair(Icons.Default.Menu, onMenuClick),
                            Pair(Icons.Default.Notifications, onNotificationClick),
                            Pair(Icons.Default.Close, onCancelDeliveryClick)
                        ).forEach { (icon, action) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = action,
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = Color(0xFF384259) // Cor mais clara quando desativado
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
            when (state) {
                is HomeScreenState.Idle -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 87.dp)
                            .align(Alignment.BottomCenter),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(Color.White, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val buttonColor = Color.Green
                            val buttonText = "START"

                            Button(
                                onClick = onStartClick,
                                shape = CircleShape,
                                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                modifier = Modifier.size(110.dp)
                            ) {
                                Text(
                                    text = buttonText,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    // Bottom info text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "It's rush hour!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF384259)
                        )
                        Text(
                            "Start listening for orders to earn more!",
                            fontSize = 14.sp,
                            color = Color(0xFF384259),
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                }

                is HomeScreenState.Listening -> {
                    if (state.incomingRequest) {
                        val requestDetails = state.requestDetails
                        val context = LocalContext.current
                        IncomingRequestCard(
                            request = requestDetails!!,
                            onAccept = {
                                viewModel.acceptRequest(
                                    requestDetails.requestId,
                                    context,
                                    requestDetails.pickupLatitude,
                                    requestDetails.pickupLongitude,
                                )
                            },
                            onDecline = { viewModel.declineRequest(requestDetails.requestId) }
                        )
                    } else {
                        // START button with white border
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 87.dp)
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val buttonColor = Color.Red
                                val buttonText = "STOP"

                                Button(
                                    onClick = onStartClick,
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                                    modifier = Modifier.size(110.dp)
                                ) {
                                    Text(
                                        text = buttonText,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Bottom info text
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.White),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Listening for orders...",
                                fontSize = 16.sp,
                                color = Color(0xFF384259),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                }

                is HomeScreenState.PickingUp -> {
                    val context = LocalContext.current
                    BottomSlideToConfirm(
                        text = "Slide to Pick Up",
                        onConfirmed = {
                            viewModel.pickupOrder(state.requestId, state.courierId, context)
                        }
                    )
                }

                is HomeScreenState.Delivering -> {
                    BottomSlideToConfirm(
                        text = "Slide to Deliver",
                        onConfirmed = {
                            viewModel.deliverOrder(state.requestId, state.courierId)
                        }
                    )
                }

                is HomeScreenState.Delivered -> {
                    DeliveryEarningsCard(
                        earnings = state.deliveryEarnings.toString(),
                        onOk = { viewModel.resetToListeningState() }
                    )
                }

                is HomeScreenState.Logout -> {
                    onLogoutClick()
                }

                is HomeScreenState.Error -> {
                    // Show error message
                    ErrorCard(
                        problem = state.problem,
                        onDismiss = { viewModel.dismissError() }
                    )
                }

                is HomeScreenState.Cancelling -> {
                    if (state.isCancelled) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Delivery cancelled successfully!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF384259)
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Button(
                                        onClick = { viewModel.resetToIdleState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                                    ) {
                                        Text("OK", color = Color.White)
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Cancel Ongoing Delivery?",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF384259)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row {
                                        Button(
                                            onClick = {
                                                viewModel.cancelDelivery(
                                                    state.courierId,
                                                    state.requestId
                                                )
                                            }, // Implemente esta função
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                        ) {
                                            Text("Cancel", color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Button(
                                            onClick = { viewModel.dismissError() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                        ) {
                                            Text("Back", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/*val httpService = HttpService(
    baseUrl = "https://liftdrop-api.isel.pt",
    client = OkHttpClient(),
    gson = GsonBuilder().create()
)
val mockJson = GsonBuilder()
    .create()
val mockHttpClient: OkHttpClient = OkHttpClient()
val homeService = RealHomeService(mockHttpClient, mockJson)
val loginService = RealLoginService(httpService)


@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockLocationService = RealLocationTrackingService(mockHttpClient, courierService = homeService, mockJson, context = LocalContext.current)
    HomeScreen(
        viewModel = HomeViewModel( homeService,
            loginService = loginService,
            preferences = UserInfoSharedPrefs(LocalContext.current),
        ),
        state = HomeScreenState(
            dailyEarnings = "12.50",
            isUserLoggedIn = true,
            isListening = false,
            incomingRequest = false,
            requestDetails = CourierRequestDetails(
                requestId = "1",
                courierId = "1",
                pickupAddress = "123 Main St",
                dropoffAddress = "456 Elm St",
                pickupLatitude = 40.7128,
                pickupLongitude = -74.0060,
                dropoffLatitude = 40.7306,
                dropoffLongitude = -73.9352,
                price = "12.50",
            ),
        ),
        onMenuClick = {},
        onNotificationClick = {},
        onStartClick = {},
        onLogoutClick = {},
        userToken = "mockToken"
    )
}
*/


