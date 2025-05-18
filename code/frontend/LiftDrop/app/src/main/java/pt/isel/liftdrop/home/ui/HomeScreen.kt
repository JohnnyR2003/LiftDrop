package pt.isel.liftdrop.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import pt.isel.liftdrop.home.model.HomeViewModel
import pt.isel.liftdrop.location.LocationServices
import android.annotation.SuppressLint
import androidx.compose.material.icons.filled.ExitToApp
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import pt.isel.liftdrop.home.model.CourierRequest
import pt.isel.liftdrop.home.model.CourierRequestDetails
import pt.isel.liftdrop.home.model.RealHomeService
import pt.isel.liftdrop.location.LocationRepositoryImpl
import pt.isel.liftdrop.login.UserInfoSharedPrefs
import pt.isel.liftdrop.login.model.RealLoginService
import pt.isel.liftdrop.services.RealLocationTrackingService

data class HomeScreenState(
    val dailyEarnings: String,
    val isUserLoggedIn: Boolean,
    val isListening: Boolean,
    val incomingRequest: CourierRequestDetails? // New!
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    state: HomeScreenState,
    userToken: String = "",
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {} // Adicionado callback para logout
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
                        text = state.dailyEarnings,
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

            // Menu + Notifications + Logout
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 87.dp, end = 7.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        IconButton(onClick = action) {
                            Icon(icon, contentDescription = null, tint = Color(0xFF384259))
                        }
                    }
                }
            }

            if (state.incomingRequest != null) {
                val context = LocalContext.current
                IncomingRequestCard(
                    request = state.incomingRequest,
                    onAccept = { viewModel.acceptRequest(
                        state.incomingRequest.requestId,
                        userToken, context,
                        state.incomingRequest.pickupLatitude,
                        state.incomingRequest.pickupLongitude,
                        state.incomingRequest.dropoffLatitude,
                        state.incomingRequest.dropoffLongitude
                    ) },
                    onDecline = { viewModel.declineRequest(state.incomingRequest.requestId) }
                )
            }
            else {
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
                        val buttonColor = if (state.isListening) Color.Red else Color.Green
                        val buttonText = if (state.isListening) "STOP" else "START"

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
                    if (state.isListening) {
                        Text(
                            text = "Listening for orders...",
                            fontSize = 16.sp,
                            color = Color(0xFF384259),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        Text(
                            "It's lunch time!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF384259)
                        )
                        Text(
                            "Check the map for the busiest restaurants",
                            fontSize = 14.sp,
                            color = Color(0xFF384259),
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

val mockJson = GsonBuilder()
    .create()
val mockHttpClient: OkHttpClient = OkHttpClient()
val homeService = RealHomeService(mockHttpClient, mockJson)
val loginService = RealLoginService(mockHttpClient, mockJson)


@SuppressLint("ViewModelConstructorInComposable")
@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockLocationService = RealLocationTrackingService(mockHttpClient, courierService = homeService, mockJson, context = LocalContext.current)
    HomeScreen(
        viewModel = HomeViewModel( homeService,
            loginService = loginService,
            userRepo = UserInfoSharedPrefs(LocalContext.current),
        ),
        state = HomeScreenState(
            dailyEarnings = "12.50",
            isUserLoggedIn = true,
            isListening = false,
            incomingRequest = null
        ),
        onMenuClick = {},
        onNotificationClick = {},
        onStartClick = {},
        onLogoutClick = {}
    )
}

