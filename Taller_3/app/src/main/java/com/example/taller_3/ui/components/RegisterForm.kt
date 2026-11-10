package com.example.taller_3.ui.components

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.example.taller_3.utils.isValidPhone

@Composable
fun RegisterForm(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onRegister: (name: String, email: String, phone: String, password: String) -> Unit = { _, _, _, _ -> },
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val emailInvalid = email.isNotEmpty() && !isValidEmail(email)
    val phoneInvalid = phone.isNotEmpty() && !isValidPhone(phone)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(stringResource(R.string.name)) },
            singleLine = true,
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )
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
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(stringResource(R.string.phone_number)) },
            singleLine = true,
            enabled = enabled,
            isError = phoneInvalid,
            supportingText = { if (phoneInvalid) Text(stringResource(R.string.invalid_phone)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            enabled = enabled,
        )
        Button(
            onClick = { onRegister(name.trim(), email, phone, password) },
            enabled = enabled && name.isNotBlank() && isValidEmail(email) &&
                    isValidPhone(phone) && isValidPassword(password),
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.register)) }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterFormPreview() {
    MaterialTheme { RegisterForm() }
}
