package pt.isel.liftdrop.shared.ui

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch

@Composable
fun PinInsertionCard(
    onPinEntered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val coroutineScope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Insira o PIN de 6 dígitos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF384259)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
            ) {
                repeat(6) { index ->
                    val char = pin.getOrNull(index)?.toString() ?: ""
                    OutlinedTextField(
                        value = char,
                        onValueChange = { value ->
                            if (value.length <= 1 && value.all { it.isDigit() }) {
                                val newPin = StringBuilder(pin)
                                if (char.isNotEmpty()) {
                                    newPin.setCharAt(index, value.firstOrNull() ?: ' ')
                                } else {
                                    if (pin.length > index) {
                                        newPin.insert(index, value)
                                    } else {
                                        newPin.append(value)
                                    }
                                }
                                pin = newPin.toString().take(6)
                                if (value.isNotEmpty() && index < 5) {
                                    coroutineScope.launch {
                                        awaitFrame()
                                        focusRequesters[index + 1].requestFocus()
                                    }
                                }
                                if (pin.length == 6) onPinEntered(pin)
                            }
                        },
                        modifier = Modifier
                            .width(48.dp) // Aumenta a largura
                            .focusRequester(focusRequesters[index]),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 22.sp, // Pode reduzir um pouco se quiser
                            textAlign = TextAlign.Center
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF43A047),
                            unfocusedBorderColor = Color(0xFF384259)
                        )
                    )
                }
            }
        }
    }
}