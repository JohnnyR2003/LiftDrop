package pt.isel.liftdrop.login.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pt.isel.liftdrop.location.LocationServices
import pt.isel.liftdrop.ui.TopBar

@Composable
fun RegisterScreen(
    state: LoginScreenState,
    onRegisterRequest: (String, String, String, String) -> Unit,
    onBackRequest: () -> Unit
) {
    val firstName = rememberSaveable { mutableStateOf("") }
    val lastName = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopBar(onBackRequested = onBackRequest) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "Create an Account", fontSize = 24.sp, color = Color(0xFF384259))

            TextFieldWithLabel("First Name", firstName)
            TextFieldWithLabel("Last Name", lastName)
            TextFieldWithLabel("Email", email, KeyboardType.Email)
            TextFieldWithLabel("Password", password, KeyboardType.Password, passwordVisible) {
                passwordVisible = !passwordVisible
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    var location: LatLng? = null
                    onRegisterRequest(firstName.value, lastName.value, email.value, password.value)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF384259),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Register")
            }

            state.error?.let {
                Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
            }

            if (state.loadingState) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Loading...", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun TextFieldWithLabel(
    label: String,
    value: MutableState<String>,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    togglePasswordVisibility: (() -> Unit)? = null
) {
    Text(text = label, fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
    TextField(
        value = value.value,
        onValueChange = { value.value = ensureInputBounds(it) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = if (isPassword && togglePasswordVisibility != null) {
            if (value.value.isNotEmpty()) PasswordVisualTransformation() else VisualTransformation.None
        } else VisualTransformation.None,
        trailingIcon = if (togglePasswordVisibility != null) {
            {
                val visible = value.value.isNotEmpty()
                IconButton(onClick = togglePasswordVisibility) {
                    Icon(
                        imageVector = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null
    )
}
