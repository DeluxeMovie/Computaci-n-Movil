package com.example.taller_3.ui.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val PASSWORD_REGEX = Regex("^(?=.*[^A-Za-z0-9\\s]).{8,}$")

private fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

private fun isValidPassword(password: String) = PASSWORD_REGEX.matches(password)

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onLogin: (email: String, password: String) -> Unit = { _, _ -> },
    onRegister: () -> Unit = {},
    onForgotPassword: (email: String) -> Unit = { _ -> },
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    val emailInvalid = email.isNotEmpty() && !isValidEmail(email)
    val passwordInvalid = password.isNotEmpty() && !isValidPassword(password)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            enabled = enabled,
            isError = emailInvalid,
            supportingText = { if (emailInvalid) Text("Enter a valid email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            enabled = enabled,
            isError = passwordInvalid,
            supportingText = {
                if (passwordInvalid) Text("At least 8 characters and one special symbol")
            },
            visualTransformation = if (showPassword) VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }, enabled = enabled) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff
                        else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onLogin(email, password) },
                enabled = enabled && isValidEmail(email) && isValidPassword(password),
                modifier = Modifier.weight(1f),
            ) { Text("Login") }
            OutlinedButton(
                onClick = onRegister,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text("Register")
            }
        }
        TextButton(
            onClick = { onForgotPassword(email) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Forgot password?", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormPreview() {
    MaterialTheme { LoginForm() }
}
