package com.example.taller_3.ui.screens

import com.example.taller_3.utils.asString
import com.example.taller_3.utils.UiText
import com.example.taller_3.R
import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.LocationSwitch
import com.example.taller_3.ui.components.UsersMap
import com.example.taller_3.utils.openAppSettings
import com.example.taller_3.viewmodels.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Pantalla principal: un mapa con la posición del usuario y la de los demás usuarios en línea,
 * y un switch para encender o apagar la localización.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var permissionRequested by rememberSaveable { mutableStateOf(false) }

    // Si el usuario concede el permiso desde el diálogo, la localización se enciende
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    ) { result ->
        if (result.values.any { it }) viewModel.startSharing(context)
        else viewModel.showError(UiText.Resource(R.string.location_permission_required))
    }
    val isLocationGranted = permissionsState.permissions.any { it.status.isGranted }
    val canAskAgain = permissionsState.permissions.any { it.status.shouldShowRationale }

    // Los usuarios en línea se escuchan solo mientras esta pantalla está visible
    DisposableEffect(viewModel) {
        viewModel.startObservingUsers()
        onDispose { viewModel.stopObservingUsers() }
    }

    Box(modifier = modifier.fillMaxSize()) {
        UsersMap(
            currentLocation = state.currentLocation,
            onlineUsers = state.onlineUsers,
        )
        LocationSwitch(
            checked = state.isSharing,
            onCheckedChange = { enabled ->
                when {
                    !enabled -> viewModel.stopSharing()
                    isLocationGranted -> viewModel.startSharing(context)
                    // Si el sistema ya no muestra el diálogo, el permiso solo se puede dar desde los ajustes
                    !permissionRequested || canAskAgain -> {
                        permissionRequested = true
                        permissionsState.launchMultiplePermissionRequest()
                    }
                    else -> openAppSettings(context)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
        ErrorMessage(
            message = state.error?.asString(),
            onDismiss = viewModel::clearError,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp),
        )
    }
}
