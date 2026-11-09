package com.example.taller_3.ui.components

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
import com.example.taller_3.utils.isValidPhone

/**
 * Muestra los datos guardados del usuario. El email es solo lectura;
 * el nombre y el teléfono se pueden modificar.
 */
@Composable
fun ProfileForm(
    name: String,
    email: String,
    phone: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSave: (name: String, phone: String) -> Unit = { _, _ -> },
) {
    // Al cambiar los datos en la base, los campos se reinician con el nuevo valor
    var editedName by rememberSaveable(name) { mutableStateOf(name) }
    var editedPhone by rememberSaveable(phone) { mutableStateOf(phone) }

    val phoneInvalid = editedPhone.isNotEmpty() && !isValidPhone(editedPhone)
    val changed = editedName.trim() != name || editedPhone != phone

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = editedName,
            onValueChange = { editedName = it },
            label = { Text("Name") },
            singleLine = true,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = email,
            onValueChange = {},
            label = { Text("Email") },
            singleLine = true,
            readOnly = true,
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = editedPhone,
            onValueChange = { editedPhone = it },
            label = { Text("Phone number") },
            singleLine = true,
            enabled = enabled,
            isError = phoneInvalid,
            supportingText = { if (phoneInvalid) Text("Enter a valid phone number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onSave(editedName, editedPhone) },
            enabled = enabled && changed && editedName.isNotBlank() && isValidPhone(editedPhone),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Save changes") }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfileFormPreview() {
    MaterialTheme { ProfileForm(name = "Jane Doe", email = "jane@mail.com", phone = "3001234567") }
}
