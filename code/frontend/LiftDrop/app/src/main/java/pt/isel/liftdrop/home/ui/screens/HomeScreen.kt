package pt.isel.liftdrop.home.ui.screens

import DeliveryEarningsCard
import OrderInfoCard
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import pt.isel.liftdrop.services.location.LocationServices
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Info
import pt.isel.liftdrop.shared.ui.BottomSlideToConfirm
import pt.isel.liftdrop.shared.ui.ErrorCard
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.ui.tooling.preview.Preview
import pt.isel.liftdrop.home.model.HomeService
import pt.isel.liftdrop.home.model.websocket.IncomingRequestDetails
import pt.isel.liftdrop.home.model.dto.LocationDTO
import pt.isel.liftdrop.home.ui.components.IncomingRequestCard
import pt.isel.liftdrop.home.ui.components.PickupSuccessCard
import pt.isel.liftdrop.login.model.LoginService
import pt.isel.liftdrop.login.preferences.PreferencesRepository
import pt.isel.liftdrop.domain.UserInfo
import pt.isel.liftdrop.login.model.dto.GetCourierIdOutputModel
import pt.isel.liftdrop.login.model.dto.LogoutOutputModel
import pt.isel.liftdrop.login.model.dto.RegisterOutputModel
import pt.isel.liftdrop.services.http.Problem
import pt.isel.liftdrop.services.http.Result
import pt.isel.liftdrop.services.location.LocationTrackingService
import pt.isel.liftdrop.shared.ui.FadingStatusCard
import pt.isel.liftdrop.shared.ui.PinInsertionCard

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    state: HomeScreenState,
    dailyEarnings: String,
    onStartClick: () -> Unit = {},
    onOrderInfoClick: () -> Unit = {},
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

            val icons = when (state) {
                is HomeScreenState.Idle -> listOf(
                    //Triple(Icons.Default.Menu, onMenuClick, false),
                    Triple(Icons.Default.ExitToApp, onLogoutClick, false)
                )
                is HomeScreenState.Listening -> listOf(
                    // Triple(Icons.Default.Menu, onMenuClick, false),
                )
                is HomeScreenState.HeadingToPickUp, is HomeScreenState.HeadingToDropOff -> listOf(
                    //Triple(Icons.Default.Menu, onMenuClick, false),
                    Triple(Icons.Default.Info, onOrderInfoClick, false),
                    Triple(Icons.Default.Close, onCancelDeliveryClick, false)
                )
                else -> emptyList()
            }

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

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 120.dp, end = 7.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (state) {
                    is HomeScreenState.Idle -> {
                        icons.forEach { (icon, action, showBadge) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = action) {
                                    if (showBadge) {
                                        BadgedBox(badge = { Badge() }) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color(0xFF384259)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF384259)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is HomeScreenState.Listening -> {
                        // Show only menu and notifications icons
                        icons.forEach { (icon, action, showBadge) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = action) {
                                    if (showBadge) {
                                        BadgedBox(badge = { Badge() }) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color(0xFF384259)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF384259)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    is HomeScreenState.HeadingToPickUp, is HomeScreenState.HeadingToDropOff -> {
                        icons.forEach { (icon, action, showBadge) ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(onClick = action) {
                                    if (showBadge) {
                                        BadgedBox(badge = { Badge() }) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color(0xFF384259)
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = Color(0xFF384259)
                                        )
                                    }
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
                            "Hora de ponta!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF384259)
                        )
                        Text(
                            "Comece a receber pedidos",
                            fontSize = 14.sp,
                            color = Color(0xFF384259),
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                }

                is HomeScreenState.Listening -> {
                    if (state.incomingRequest) {
                        val requestDetails = state.requestDetails
                        IncomingRequestCard(
                            request = requestDetails!!,
                            onAccept = {
                                viewModel.acceptRequest(
                                    requestDetails.requestId,
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

                is HomeScreenState.HeadingToPickUp -> {
                    val context = LocalContext.current

                    Box(modifier = Modifier.fillMaxSize()) {
                        // Botão de informações no canto superior direito

                        if (!state.isPickUpSpotValid) {
                            if(state.isOrderInfoVisible){
                                OrderInfoCard(
                                    requestDetails = viewModel.currentRequest.value!!,
                                    onClose = { viewModel.toggleOrderInfoVisibility() },
                                )
                            }
                            BottomSlideToConfirm(
                                text = "Deslize para recolher",
                                onConfirmed = {
                                    viewModel.validatePickup(
                                        viewModel.currentRequest.value!!.requestId,
                                        viewModel.currentRequest.value!!.courierId,
                                        context
                                    )
                                },
                            )
                        } else {
                            if(state.isOrderInfoVisible){
                                OrderInfoCard(
                                    requestDetails = viewModel.currentRequest.value!!,
                                    onClose = { viewModel.toggleOrderInfoVisibility() },
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp)
                                    .align(Alignment.BottomCenter),
                                contentAlignment = Alignment.Center
                            ) {
                                PinInsertionCard(
                                    orderNumber = viewModel.currentRequest.value!!.requestId,
                                    onPinEntered = { pin ->
                                        viewModel.validatePickUpPin(
                                            viewModel.currentRequest.value!!.requestId,
                                            viewModel.currentRequest.value!!.courierId,
                                            pin,
                                            viewModel.currentRequest.value!!.deliveryKind,
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                is HomeScreenState.PickedUp -> {
                    val context = LocalContext.current
                    PickupSuccessCard(
                        onOk = {
                            viewModel.startNavigationToDropOff(context = context)
                        }
                    )
                }

                is HomeScreenState.HeadingToDropOff -> {
                    if (!state.isDropOffSpotValid) {
                        if(state.isOrderInfoVisible){
                            OrderInfoCard(
                                requestDetails = viewModel.currentRequest.value!!,
                                onClose = { viewModel.toggleOrderInfoVisibility() },
                            )
                        }
                        BottomSlideToConfirm(
                            text = "Deslize para entregar",
                            onConfirmed = {
                                viewModel.validateDropOff(
                                    viewModel.currentRequest.value!!.requestId,
                                    viewModel.currentRequest.value!!.courierId
                                )
                            }
                        )
                    } else {
                        if(state.isOrderInfoVisible){
                            OrderInfoCard(
                                requestDetails = viewModel.currentRequest.value!!,
                                onClose = { viewModel.toggleOrderInfoVisibility() },
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 32.dp)
                                .align(Alignment.BottomCenter),
                            contentAlignment = Alignment.Center
                        ) {
                            PinInsertionCard(
                                onPinEntered = { pin ->
                                    viewModel.validateDropOffPin(
                                        viewModel.currentRequest.value!!.requestId,
                                        viewModel.currentRequest.value!!.courierId,
                                        pin,
                                        viewModel.currentRequest.value!!.deliveryEarnings.toDouble()
                                    )
                                }
                            )
                        }
                    }
                }

                is HomeScreenState.Delivered -> {
                    DeliveryEarningsCard(
                        earnings = viewModel.currentRequest.value!!.deliveryEarnings,
                        onOk = {
                            viewModel.resetToListeningState()
                        }
                    )
                }

                is HomeScreenState.Logout -> {
                    onLogoutClick()
                }

                is HomeScreenState.Error -> {
                    ErrorCard(
                        problem = state.problem,
                        onDismiss = { viewModel.dismissError() }
                    )
                }

                is HomeScreenState.CancellingOrder -> {
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
                                    "Cancelar entrega em curso?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF384259)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row {
                                    Button(
                                        onClick = {
                                            viewModel.cancelDelivery(
                                                viewModel.currentRequest.value!!.courierId,
                                                viewModel.currentRequest.value!!.requestId,
                                                state.deliveryStatus,
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                    ) {
                                        Text("Cancelar", color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Button(
                                        onClick = { viewModel.dismissError() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                                    ) {
                                        Text("Voltar", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                is HomeScreenState.CancellingPickup -> {
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
                                    "Entrega cancelada com sucesso!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF384259)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { viewModel.resetToIdleState() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(
                                            0xFF43A047
                                        )
                                    )
                                ) {
                                    Text("OK", color = Color.White)
                                }
                            }
                        }
                    }
                }

                is HomeScreenState.CancellingDropOff -> {
                    if (state.isOrderReassigned) {
                        if(state.isOrderPickedUp) {
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
                                            "Entrega cancelada com sucesso!",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF384259)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = { viewModel.resetToIdleState() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFF43A047
                                                )
                                            )
                                        ) {
                                            Text("OK", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                        else {
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
                                            "Forneça o seguinte código ao novo estafeta:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF384259)
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = "${state.pickupCode}",
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            modifier = Modifier
                                                .background(
                                                    Color(0xFF384259),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .padding(16.dp)
                                        )

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
                                        "A aguardar pela reatribuição da encomenda...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color(0xFF384259)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }
                    }
                }
                is HomeScreenState.RequestAccepted -> {
                    val context = LocalContext.current
                    FadingStatusCard(
                        message = state.message,
                        backgroundColor = Color(0xFF43A047), // Verde
                        onFadeOut = {
                            viewModel.headToPickUp(
                                context = context,
                                pickupLat = viewModel.currentRequest.value!!.pickupLatitude,
                                pickupLon = viewModel.currentRequest.value!!.pickupLongitude,
                            )
                        }
                    )
                }

                is HomeScreenState.RequestDeclined -> {
                    FadingStatusCard(
                        message = state.message,
                        backgroundColor = Color(0xFFD32F2F), // Vermelho
                        onFadeOut = { viewModel.resetToListeningState() }
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Idle")
@Composable
fun HomeScreenPreviewIdle() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.Idle(dailyEarnings = "25.00"),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Listening")
@Composable
fun HomeScreenPreviewListening() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.Listening(dailyEarnings = "30.00", incomingRequest = false, requestDetails = null),
        dailyEarnings = "30.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "IncomingRequestCard")
@Composable
fun HomeScreenIncomingRequestCardPreview() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.Listening(
            dailyEarnings = "30.00",
            incomingRequest = true,
            requestDetails = IncomingRequestDetails(
                requestId = "123",
                courierId = "456",
                pickupLatitude = 38.7169,
                pickupLongitude = -9.1399,
                pickupAddress = "Rua de Exemplo, 123",
                dropoffAddress = "Rua de Exemplo, 456",
                dropoffLatitude = 38.7169,
                dropoffLongitude = -9.1399,
                item = "Pizza",
                quantity = 1,
                deliveryKind = "Standard",
                deliveryEarnings = "5.37"
            )
        ),
        dailyEarnings = "30.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "HeadingToPickUp")
@Composable
fun HomeScreenPreviewHeadingToPickUp() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.HeadingToPickUp(isPickUpSpotValid = false, isOrderInfoVisible = false),
        dailyEarnings = "40.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "PickedUp")
@Composable
fun HomeScreenPreviewPickedUp() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.PickedUp(),
        dailyEarnings = "50.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "HeadingToDropOff")
@Composable
fun HomeScreenPreviewHeadingToDropOff() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.HeadingToDropOff(isDropOffSpotValid = false, isOrderInfoVisible = false),
        dailyEarnings = "60.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Delivered")
@Composable
fun HomeScreenPreviewDelivered() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.Delivered(dailyEarnings = "25.00"),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Error")
@Composable
fun HomeScreenPreviewError() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.Error(problem = Problem(
            type = "problem-type",
            title = "Courier Not Near Drop Off",
            detail = "You're not near the drop-off location.",
            status = 500
        )
        ),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "CancellingOrder")
@Composable
fun HomeScreenPreviewCancellingOrder() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.CancellingOrder(deliveryStatus = "HEADING_TO_PICKUP"),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "CancellingPickup")
@Composable
fun HomeScreenPreviewCancellingPickup() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.CancellingPickup(),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "CancellingDropOff")
@Composable
fun HomeScreenPreviewCancellingDropOff() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.CancellingDropOff(isOrderReassigned = false, pickUpLocation = null),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "CancellingDropOffReassigned")
@Composable
fun HomeScreenPreviewCancellingDropOffAssigned() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.CancellingDropOff(isOrderReassigned = true, pickUpLocation = LocationDTO(
            latitude = 38.7169,
            longitude = -9.1399
        ),
            pickupCode = "1234"),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "CancellingDropOffPickedUp")
@Composable
fun HomeScreenPreviewCancellingDropOffPickedUp() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.CancellingDropOff(isOrderReassigned = true, isOrderPickedUp = true, pickUpLocation = null),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "RequestAccepted")
@Composable
fun HomeScreenPreviewRequestAccepted() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.RequestAccepted(message = "Request accepted successfully!"),
        dailyEarnings = "25.00"
    )
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "RequestDeclined")
@Composable
fun HomeScreenPreviewRequestDeclined() {
    HomeScreen(
        viewModel = FakeHomeViewModel,
        state = HomeScreenState.RequestDeclined(message = "Request declined!"),
        dailyEarnings = "25.00"
    )
}

val FakeHomeViewModel = HomeViewModel(
    homeService = FakeHomeService(),
    loginService = FakeLoginService(),
    locationServices = FakeLocationServices(),
    preferences = FakePreferencesRepository()
)

class FakeHomeService : HomeService {
    override suspend fun startListening(token: String, onMessage: (String) -> Unit, onFailure: (Throwable) -> Unit) {}
    override suspend fun stopListening() {}
    override suspend fun acceptRequest(requestId: String, token: String): Boolean = true
    override suspend fun declineRequest(requestId: String): Boolean = true
    override suspend fun validatePickup(requestId: String, courierId: String, token: String): Result<Boolean> = Result.Success(true)
    override suspend fun pickupOrder(requestId: String, courierId: String, pickUpPin: String, deliveryKind: String, token: String): Result<Boolean> = Result.Success(true)
    override suspend fun validateDropOff(requestId: String, courierId: String, token: String): Result<Boolean> = Result.Success(true)
    override suspend fun deliverOrder(requestId: String, courierId: String, dropOffPin: String, deliveryEarnings: Double, token: String): Result<Boolean> =  Result.Success(true)
    override suspend fun cancelDelivery(courierId: String, requestId: String, deliveryStatus: String, pickUpLocation: LocationDTO?, token: String): Result<Boolean> = Result.Success(true)
    override suspend fun getDailyEarnings(courierId: String, token: String): Result<Double> = Result.Success(100.0)
    override suspend fun getCourierIdByToken(token: String): Result<Int> = Result.Success(1)
}

class FakeLocationServices : LocationTrackingService{
    override suspend fun getCurrentLocation(): Result<Location> =
        Result.Success(Location("FakeProvider").apply {
            latitude = 38.7169
            longitude = -9.1399
        })

    override fun startUpdating(authToken: String, courierId: String) = Unit

    override fun stopUpdating() = Unit

    override suspend fun sendLocationToBackend(
        lat: Double,
        lon: Double,
        courierId: String,
        authToken: String
    ): Result<Boolean> = Result.Success(true)

}

class FakeLoginService : LoginService {
    override suspend fun register(email: String, password: String, username: String): Result<RegisterOutputModel> = Result.Success(RegisterOutputModel(1))
    override suspend fun login(username: String, password: String): Result<UserInfo> = Result.Success(UserInfo(1, "token", "username", "email"))
    override suspend fun logout(token: String, courierId: String): Result<LogoutOutputModel> = Result.Success(LogoutOutputModel(false))
    override suspend fun getCourierIdByToken(token: String): Result<GetCourierIdOutputModel> = Result.Success(GetCourierIdOutputModel("1"))
}

class FakePreferencesRepository : PreferencesRepository {
    override suspend fun isLoggedIn(): Boolean = true

    override suspend fun getUserInfo(): UserInfo? = UserInfo(1, "token", "username", "email")

    override suspend fun setUserInfo(userInfo: UserInfo) = Unit

    override suspend fun clearUserInfo(userInfo: UserInfo) = Unit

}
