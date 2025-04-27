package pt.isel.liftdrop.delivery.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import pt.isel.liftdrop.location.LocationServices

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ✅ Fullscreen map
        LocationServices().LocationAwareMap()

        // ✅ Retângulo arredondado para ganhos diários (aumentado)
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "120.50",
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

        // ✅ IconButtons dentro de círculos brancos
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 87.dp, bottom = 32.dp, end = 7.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color(0xFF384259))
                }
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF384259))
                }
            }
        }

        // ✅ STOP Button com contorno circular branco
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 87.dp)
                .align(Alignment.BottomCenter),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp) // Tamanho maior para o contorno
                    .background(Color.White, shape = CircleShape), // Contorno branco
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { /*TODO*/ },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    modifier = Modifier.size(110.dp) // Tamanho do botão interno
                ) {
                    Text("STOP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        // ✅ Bottom Text just below button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 0.dp)
                .align(Alignment.BottomCenter)
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Waiting for orders...", fontWeight = FontWeight.Bold, color = Color(0xFF384259), fontSize = 28.sp)
            Text(
                "",
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color.White)
                    .padding(bottom = 5.dp)
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun PreviewWaitingScreen() {
    WaitingScreen()
}
