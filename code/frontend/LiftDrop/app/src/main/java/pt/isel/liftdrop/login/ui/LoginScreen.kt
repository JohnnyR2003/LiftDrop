package pt.isel.liftdrop.login.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.liftdrop.R
import pt.isel.liftdrop.login.model.Token
import pt.isel.liftdrop.ui.TopBar

data class LoginScreenState(
    val token: Token? = null,
    val loadingState: Boolean = false,
    val error: String? = null,
)

@Composable
fun LoginScreen(
    state: LoginScreenState = LoginScreenState(),
    initialEmail: String = "",
    initialPassword: String = "",
    onSignInRequest: ((String, String) -> Unit)? = null,
    onBackRequest: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        /* topBar = {
             TopBar(onBackRequested = { onBackRequest() })
         },*/
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if (!state.loadingState) {
                Image(
                    painter = painterResource(id = R.drawable.logold),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(160.dp)
                        .width(300.dp)
                )

                //Text(text = "Login", fontSize = 24.sp, color = Color(0xFF384259))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Enter your email", fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
                TextField(
                    value = email.value,
                    onValueChange = { email.value = ensureInputBounds(it) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Enter your password", fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
                TextField(
                    value = password.value,
                    onValueChange = { password.value = ensureInputBounds(it) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forgot password?",
                    fontSize = 14.sp,
                    color = Color(0xFF384259),
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = { onSignInRequest?.invoke(email.value, password.value) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF384259),
                        contentColor = Color.White
                    ),
                ){
                    Text(text = "Login")
                }

                state.error?.let {
                    Toast.makeText(LocalContext.current, it, Toast.LENGTH_LONG).show()
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Don't have an account? Sign up",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("or", fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Gray)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Handle Google login */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google Logo",
                        modifier = Modifier.height(24.dp).width(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Continue with Google")
                }
            } else {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private const val MAX_INPUT_SIZE = 32
internal fun ensureInputBounds(input: String) = input.take(MAX_INPUT_SIZE)

