package com.example.taller_3.ui.screens

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ForgotPasswordScreen(
    modifier: Modifier = Modifier,
) {
    Text(text = stringResource(R.string.forgot_password_screen), modifier = modifier)
}