package pt.isel.liftdrop.home.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import pt.isel.liftdrop.home.model.IncomingRequestDetails

@Composable
fun IncomingRequestCard(
    request: IncomingRequestDetails,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    timeoutSeconds: Int = 20
) {
    var timeLeft by remember { mutableStateOf(timeoutSeconds) }
    val progress by remember(timeLeft) { derivedStateOf { timeLeft / timeoutSeconds.toFloat() } }

    val targetColor = when {
        progress > 0.66f -> Color(0xFF00C853)
        progress > 0.33f -> Color(0xFFFFC107)
        else -> Color(0xFFD32F2F)
    }
    val animatedBarColor by animateColorAsState(targetValue = targetColor, label = "ProgressBarColor")

    LaunchedEffect(request.requestId) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) onDecline()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Barra de tempo no topo
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                        color = animatedBarColor,
                        trackColor = Color(0xFFEEEEEE)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Valor em destaque
                    Text(
                        text = "+${request.deliveryEarnings}€",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp,
                        color = Color(0xFF384259),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Text(
                        text = "Ganhos estimados",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Endereços
                    // Substitua o bloco de endereços por:
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Endereço de recolha
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Pickup",
                                tint = Color(0xFF43A047),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = request.pickupAddress,
                                fontSize = 16.sp,
                                color = Color(0xFF384259),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Linha tracejada vertical centralizada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DashedVerticalDivider(
                                color = Color(0xFFE0E0E0),
                                modifier = Modifier
                                    .height(32.dp)
                                    .width(2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Endereço de entrega
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Dropoff",
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = request.dropoffAddress,
                                fontSize = 16.sp,
                                color = Color(0xFF384259),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Botões
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = onDecline,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Recusar", color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = onAccept,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.weight(2f)
                        ) {
                            Text("Aceitar", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DashedVerticalDivider(
    color: Color = Color(0xFFE0E0E0),
    modifier: Modifier = Modifier,
    dashHeight: Float = 8f,
    gapHeight: Float = 6f
) {
    Box(
        modifier = modifier
            .width(1.dp)
            .fillMaxHeight()
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = color,
                    start = Offset(x = size.width / 2, y = y),
                    end = Offset(x = size.width / 2, y = y + dashHeight),
                    strokeWidth = size.width,
                    cap = Stroke.DefaultCap
                )
                y += dashHeight + gapHeight
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IncomingRequestCardPreview() {
    IncomingRequestCard(
        request = IncomingRequestDetails(
            courierId = "1",
            requestId = "1",
            pickupLatitude = 38.7169,
            pickupLongitude = -9.1399,
            dropoffLatitude = 38.7169,
            dropoffLongitude = -9.1399,
            pickupAddress = "123 Main St, City",
            dropoffAddress = "456 Elm St, City",
            deliveryEarnings = "15.0",
            deliveryKind = "DEFAULT",
            item = "Pizza",
            quantity = 2
        ),
        onAccept = {},
        onDecline = {}
    )
}

