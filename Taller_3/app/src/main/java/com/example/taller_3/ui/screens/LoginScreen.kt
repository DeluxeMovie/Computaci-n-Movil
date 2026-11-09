package com.example.taller_3.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.viewmodels.AuthEvent
import com.example.taller_3.viewmodels.AuthState
import com.example.taller_3.viewmodels.AuthViewModel
import com.example.taller_3.R
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.LoginForm

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    goHome: () -> Unit = {},
    goRegister: () -> Unit = {},
    goForgotPassword: () -> Unit = {},
    viewModel: AuthViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.NavigateToHome) goHome()
        }
    }

    Box(
        modifier = modifier.fillMaxSize().safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(220.dp).clip(RoundedCornerShape(28.dp))) {
                Image(
                    painter = painterResource(R.drawable.ic_launcher_foreground),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize(),
                )
            }
            when (val s = state) {
                is AuthState.Loading -> CircularProgressIndicator(Modifier.padding(8.dp))
                else -> Unit
            }
            ErrorMessage(
                message = (state as? AuthState.Error)?.message,
                onDismiss = viewModel::clearError,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            LoginForm(
                enabled = state !is AuthState.Loading,
                onLogin = { email, password ->
                    viewModel.clearError()
                    viewModel.login(email, password)
                },
                onRegister = goRegister,
                onForgotPassword = { email ->
                    viewModel.clearError()
                    viewModel.forgotPassword(email)
                },
            )
        }
    }
}
