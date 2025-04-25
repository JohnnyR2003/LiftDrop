package pt.isel.liftdrop.notifications.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    private val _notifications = mutableStateOf<List<String>>(emptyList())
    val notifications: State<List<String>> = _notifications

    init {
        fetchNotifications()
    }

    private fun fetchNotifications() {
        viewModelScope.launch {
            // Simula uma chamada ao backend
            delay(2000) // Simula tempo de resposta
            _notifications.value = listOf(
                "Nova mensagem recebida",
                "Seu pedido foi enviado",
                "Atualização disponível"
            )
        }
    }

    fun removeNotification(notification: String) {
        _notifications.value = _notifications.value.filter { it != notification }
    }
}