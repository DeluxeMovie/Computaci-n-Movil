package com.example.taller_3.ui.screens

import com.example.taller_3.utils.asString
import com.example.taller_3.R
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.ui.components.ChangePasswordForm
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.ProfileForm
import com.example.taller_3.ui.components.ProfilePhoto
import com.example.taller_3.viewmodels.ProfileEvent
import com.example.taller_3.viewmodels.ProfileState
import com.example.taller_3.viewmodels.ProfileViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val user by viewModel.user.collectAsState()
    val state by viewModel.state.collectAsState()
    // Al cambiar esta llave el formulario de contraseña se vacía
    var passwordFormKey by remember { mutableIntStateOf(0) }

    DisposableEffect(viewModel) {
        viewModel.clearError()
        viewModel.startObserving()
        onDispose { viewModel.stopObserving() }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            val message = when (event) {
                ProfileEvent.ProfileUpdated -> context.getString(R.string.profile_updated)
                ProfileEvent.PhotoUpdated -> context.getString(R.string.photo_updated)
                ProfileEvent.PasswordChanged -> {
                    passwordFormKey++
                    context.getString(R.string.password_changed)
                }
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state is ProfileState.Loading || user == null) {
            CircularProgressIndicator(Modifier.padding(8.dp))
        }
        ErrorMessage(
            message = (state as? ProfileState.Error)?.message?.asString(),
            onDismiss = viewModel::clearError,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        user?.let { currentUser ->
            ProfilePhoto(
                imageUrl = currentUser.photoUrl,
                name = currentUser.name,
                enabled = state !is ProfileState.Loading,
                onPhotoSelected = { uri ->
                    viewModel.clearError()
                    viewModel.updatePhoto(uri)
                },
            )
            ProfileForm(
                name = currentUser.name,
                email = currentUser.email,
                phone = currentUser.phone,
                enabled = state !is ProfileState.Loading,
                onSave = { name, phone ->
                    viewModel.clearError()
                    viewModel.updateProfile(name, phone)
                },
            )
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            key(passwordFormKey) {
                ChangePasswordForm(
                    enabled = state !is ProfileState.Loading,
                    onChangePassword = { current, new ->
                        viewModel.clearError()
                        viewModel.changePassword(current, new)
                    },
                )
            }
        }
    }
}
