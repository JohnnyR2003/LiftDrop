import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pt.isel.liftdrop.R
import pt.isel.liftdrop.notifications.model.NotificationViewModel

@Composable
fun NotificationScreen(viewModel: NotificationViewModel = viewModel()) {
    val notifications = viewModel.notifications.value

    if (notifications.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "You have no notifications",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Notificações",
                fontSize = 20.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            notifications.forEach { notification ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = Color.LightGray.copy(alpha = 0.3f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    IconButton(onClick = { viewModel.removeNotification(notification) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.closeicon), // Substitua pelo ícone "X"
                            contentDescription = "Delete notification",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}