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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.liftdrop.R
import pt.isel.liftdrop.domain.login.Login.emailLabel
import pt.isel.liftdrop.domain.login.Login.forgotPasswordText
import pt.isel.liftdrop.domain.login.Login.googleLoginText
import pt.isel.liftdrop.domain.login.Login.loginFailedMessage
import pt.isel.liftdrop.domain.login.Login.noAccountMessage
import pt.isel.liftdrop.domain.login.Login.passwordLabel
import pt.isel.liftdrop.domain.login.Login.submitButtonText
import pt.isel.liftdrop.domain.register.Email
import pt.isel.liftdrop.domain.register.Password


@Composable
fun LoginScreen(
    isLoggingIn: Boolean = false,
    screenState: LoginScreenState,
    onSignInRequest: ((String, String) -> Unit)? = null,
    onNavigateToRegister: () -> Unit
) {
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
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
            if (!isLoggingIn) {
                Image(
                    painter = painterResource(id = R.drawable.logold),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(160.dp)
                        .width(300.dp)
                )

                //Text(text = "Login", fontSize = 24.sp, color = Color(0xFF384259))

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(emailLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
                TextField(
                    value = email.value,
                    onValueChange = { email.value = ensureInputBounds(it) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(passwordLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
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
                    text = stringResource(forgotPasswordText),
                    fontSize = 14.sp,
                    color = Color(0xFF384259),
                    modifier = Modifier.align(Alignment.End)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(enabled = Email.isValid(email.value.trim()) &&
                        Password.isValid(password.value.trim()),
                    onClick = { onSignInRequest?.invoke(email.value.trim(), password.value.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF384259),
                        contentColor = Color.White
                    ),
                ){
                    Text(text = stringResource(submitButtonText))
                }

                if (screenState is LoginScreenState.Error) {
                    Text(
                        stringResource(loginFailedMessage),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = stringResource(noAccountMessage),
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
                    Text(text = stringResource(googleLoginText))
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

