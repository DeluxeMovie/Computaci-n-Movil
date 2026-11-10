package com.example.taller_3.ui.components

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taller_3.utils.isValidEmail
import com.example.taller_3.utils.isValidPassword

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

    val emailInvalid = email.isNotEmpty() && !isValidEmail(email)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email)) },
            singleLine = true,
            enabled = enabled,
            isError = emailInvalid,
            supportingText = { if (emailInvalid) Text(stringResource(R.string.invalid_email)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            enabled = enabled,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { onLogin(email, password) },
                enabled = enabled && isValidEmail(email) && isValidPassword(password),
                modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.login)) }
            OutlinedButton(
                onClick = onRegister,
                enabled = enabled,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.register))
            }
        }
        TextButton(
            onClick = { onForgotPassword(email) },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.forgot_password), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormPreview() {
    MaterialTheme { LoginForm() }
}
