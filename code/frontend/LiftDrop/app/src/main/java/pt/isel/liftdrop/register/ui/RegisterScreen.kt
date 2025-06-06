package pt.isel.liftdrop.register.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pt.isel.liftdrop.domain.IOState
import pt.isel.liftdrop.domain.register.Email
import pt.isel.liftdrop.domain.register.Password
import pt.isel.liftdrop.domain.register.Register
import pt.isel.liftdrop.login.ui.LoginScreenState
import pt.isel.liftdrop.login.ui.ensureInputBounds
import pt.isel.liftdrop.ui.TopBar

@Composable
fun RegisterScreen(
    state: RegisterScreenState,
    onRegisterRequest: (String, String, String, String) -> Unit,
    onBackRequest: () -> Unit
) {
    val firstName = rememberSaveable { mutableStateOf("") }
    val lastName = rememberSaveable { mutableStateOf("") }
    val email = rememberSaveable { mutableStateOf("") }
    val password = rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = { TopBar(onBackRequested = onBackRequest) },
        containerColor = Color.White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(scrollState)
                .imePadding()
                .nestedScroll(rememberNestedScrollInteropConnection()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(text = stringResource(Register.title), fontSize = 24.sp, color = Color(0xFF384259))

            Spacer(modifier = Modifier.height(24.dp))

            Text(text = stringResource(Register.firstNameLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
            TextField(
                value = firstName.value,
                onValueChange = { firstName.value = ensureInputBounds(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(Register.lastNameLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
            TextField(
                value = lastName.value,
                onValueChange = { lastName.value = ensureInputBounds(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(Register.emailLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
            TextField(
                value = email.value,
                onValueChange = { email.value = ensureInputBounds(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = stringResource(Register.passwordLabel), fontSize = 14.sp, color = Color(0xFF384259), modifier = Modifier.align(Alignment.Start))
            TextField(
                value = password.value,
                onValueChange = { password.value = ensureInputBounds(it) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                enabled = Email.isValid(email.value.trim()) &&
                        Password.isValid(password.value.trim())
                && firstName.value.isNotBlank() && lastName.value.isNotBlank(),
                onClick = {
                    onRegisterRequest(firstName.value.trim(), lastName.value.trim(), email.value.trim(), password.value.trim())
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF384259),
                    contentColor = Color.White
                )
            ) {
                Text(text = stringResource(Register.submitTextButton))
            }

            if (state.isFail()) {
                Text(
                    text = stringResource(Register.invalidCredentials),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            if (state.isLoading()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Loading...", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


