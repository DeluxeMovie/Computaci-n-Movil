package com.example.taller_3.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.taller_3.utils.isValidPassword

@Composable
fun ChangePasswordForm(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onChangePassword: (currentPassword: String, newPassword: String) -> Unit = { _, _ -> },
) {
    var currentPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Change password", style = MaterialTheme.typography.titleLarge)
        PasswordField(
            value = currentPassword,
            onValueChange = { currentPassword = it },
            enabled = enabled,
            label = "Current password",
            validate = false,
        )
        PasswordField(
            value = newPassword,
            onValueChange = { newPassword = it },
            enabled = enabled,
            label = "New password",
        )
        Button(
            onClick = { onChangePassword(currentPassword, newPassword) },
            enabled = enabled && currentPassword.isNotEmpty() && isValidPassword(newPassword),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Change password") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChangePasswordFormPreview() {
    MaterialTheme { ChangePasswordForm() }
}
