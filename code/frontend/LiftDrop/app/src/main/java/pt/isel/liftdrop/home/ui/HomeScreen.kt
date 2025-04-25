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
import pt.isel.liftdrop.services.LocationServices

data class HomeScreenState(
    val dailyEarnings: String= "0.00",
    val isUserLoggedIn: Boolean= false,
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    state: HomeScreenState = HomeScreenState(),
    onMenuClick: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onStartClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {},
        containerColor = Color.White
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

            // Menu + Notifications
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 87.dp, end = 7.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        IconButton(onClick = action) {
                            Icon(icon, contentDescription = null, tint = Color(0xFF384259))
                        }
                    }
                }
            }

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
                    Button(
                        onClick = onStartClick,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                        modifier = Modifier.size(110.dp)
                    ) {
                        Text(
                            text = "START",
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
                Text("It's lunch time!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF384259))
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

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun HomePreview() {
    HomeScreen()
}

