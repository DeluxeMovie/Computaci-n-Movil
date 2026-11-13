package com.example.taller_3.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.R
import com.example.taller_3.data.UserInfo
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.ProfileImage
import com.example.taller_3.ui.components.RegisterForm
import com.example.taller_3.utils.UiText
import com.example.taller_3.viewmodels.AuthEvent
import com.example.taller_3.viewmodels.AuthState
import com.example.taller_3.viewmodels.AuthViewModel
import com.example.taller_3.viewmodels.FirestoreEvent
import com.example.taller_3.viewmodels.FirestoreViewModel
import com.example.taller_3.viewmodels.StorageEvent
import com.example.taller_3.viewmodels.StorageViewModel

@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    goHome: () -> Unit = {},
    authViewModel: AuthViewModel = viewModel(),
    firestoreViewModel: FirestoreViewModel = viewModel(),
    storageViewModel: StorageViewModel = viewModel(),
) {
    val authState by authViewModel.state.collectAsState()
    val firestoreState by firestoreViewModel.state.collectAsState()
    val storageState by storageViewModel.state.collectAsState()

    val busy = authState is AuthState.Loading || firestoreState.loading || storageState.uploading
    var pendingProfile by remember { mutableStateOf<UserInfo?>(null) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var profileError by remember { mutableStateOf<UiText?>(null) }

    LaunchedEffect(firestoreViewModel) {
        firestoreViewModel.events.collect { event ->
            if (event is FirestoreEvent.Error) profileError = event.message
        }
    }

    LaunchedEffect(storageViewModel) {
        storageViewModel.events.collect { event ->
            if (event is StorageEvent.Error) profileError = event.message
        }
    }

    LaunchedEffect(authViewModel) {
        // Se limpia cualquier error que haya quedado de la pantalla de login
        authViewModel.clearError()
        authViewModel.events.collect { event ->
            if (event !is AuthEvent.NavigateToHome) return@collect
            val uid = (authViewModel.state.value as? AuthState.Authenticated)?.user?.uid
            val profile = pendingProfile
            val photo = photoUri
            if (uid != null && profile != null && photo != null) {
                // El perfil solo se guarda cuando la imagen ya tiene una URL de Firebase Storage.
                storageViewModel.uploadUserFile(uid, "profile_image.jpg", photo) { url ->
                    if (url == null) return@uploadUserFile
                    firestoreViewModel.saveUser(uid, profile.copy(photo = url)) { ok ->
                        if (ok) goHome()
                        else if (profileError == null) profileError = UiText.Resource(R.string.register_screen_profile_save_error_message)
                    }
                }
            } else profileError = UiText.Resource(R.string.register_screen_profile_save_error_message)
        }
    }

    // El contenido se centra en la pantalla; si no cabe (teclado abierto) se puede deslizar
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = maxHeight)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 480.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.register_screen_title_text),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                ProfileImage(
                    onImageCaptured = { photoUri = it },
                    enabled = !busy,
                    modifier = Modifier.padding(8.dp),
                )
                if (busy) {
                    CircularProgressIndicator(Modifier.padding(8.dp))
                }
                ErrorMessage(
                    message = ((authState as? AuthState.Error)?.message ?: profileError)?.asString(),
                    onDismiss = {
                        authViewModel.clearError()
                        profileError = null
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                RegisterForm(
                    enabled = !busy,
                    registerEnabled = photoUri != null,
                    onRegister = { email, displayName, phone, password ->
                        authViewModel.clearError()
                        profileError = null
                        pendingProfile = UserInfo(
                            displayName = displayName,
                            phone = phone.filter { it.isDigit() }.toLongOrNull() ?: 0L,
                        )
                        authViewModel.register(email, password)
                    },
                )
            }
        }
    }
}
