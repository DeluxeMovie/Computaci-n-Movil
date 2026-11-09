package com.example.taller_3.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    goHome: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    Text(text = "Register Screen", modifier = modifier)
}