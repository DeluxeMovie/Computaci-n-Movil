package com.example.taller_3.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.RegisterForm
import com.example.taller_3.viewmodels.AuthEvent
import com.example.taller_3.viewmodels.AuthState
import com.example.taller_3.viewmodels.AuthViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    goHome: () -> Unit = {},
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        // Se limpia cualquier error que haya quedado de la pantalla de login
        viewModel.clearError()
        viewModel.events.collect { event ->
            if (event is AuthEvent.NavigateToHome) goHome()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state is AuthState.Loading) CircularProgressIndicator(Modifier.padding(8.dp))
        ErrorMessage(
            message = (state as? AuthState.Error)?.message,
            onDismiss = viewModel::clearError,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        RegisterForm(
            enabled = state !is AuthState.Loading,
            onRegister = { name, email, phone, password ->
                viewModel.clearError()
                viewModel.register(name, email, phone, password)
            },
        )
    }
}
