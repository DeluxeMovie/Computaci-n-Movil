package com.example.taller_2.ui.screens

import android.Manifest
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_2.R
import com.example.taller_2.ui.components.CameraRegion
import com.example.taller_2.ui.components.MapRegion
import com.example.taller_2.utils.openAppSettings
import com.example.taller_2.viewmodels.LocationViewModel
import com.example.taller_2.viewmodels.RoutePhotosViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Pantalla principal para tablets. Tiene dos regiones: el mapa y la cámara.
 * El mapa ocupa la mayor parte de la pantalla a la izquierda y la cámara, con la lista
 * de fotos debajo, queda a la derecha. En orientación vertical (portrait) el mapa ocupa
 * un poco menos de ancho que en horizontal (landscape).
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    locationViewModel: LocationViewModel = viewModel(),
    photosViewModel: RoutePhotosViewModel = viewModel()
) {
    val context = LocalContext.current
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT

    val locationState by locationViewModel.state.collectAsState()
    val photosState by photosViewModel.uiState.collectAsState()

    // Se piden todos los permisos juntos al iniciar la aplicación
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    val cameraPermission = permissionsState.permissions
        .first { it.permission == Manifest.permission.CAMERA }
    val finePermission = permissionsState.permissions
        .first { it.permission == Manifest.permission.ACCESS_FINE_LOCATION }
    val coarsePermission = permissionsState.permissions
        .first { it.permission == Manifest.permission.ACCESS_COARSE_LOCATION }

    val isCameraGranted = cameraPermission.status.isGranted
    val isLocationGranted = finePermission.status.isGranted || coarsePermission.status.isGranted

    // Configura la localización cuando el permiso es concedido
    LaunchedEffect(isLocationGranted) {
        if (isLocationGranted) {
            locationViewModel.setup(context)
        }
    }

    // Si el sistema aún permite pedir el permiso se vuelve a pedir, si no se abren los ajustes
    val cameraButtonText = stringResource(
        if (cameraPermission.status.shouldShowRationale) R.string.grant_permission
        else R.string.open_settings
    )
    val locationButtonText = stringResource(
        if (finePermission.status.shouldShowRationale || coarsePermission.status.shouldShowRationale)
            R.string.grant_permission
        else R.string.open_settings
    )

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MapRegion(
            isPermissionGranted = isLocationGranted,
            isRecording = locationState.isRecording,
            route = locationState.route,
            photos = photosState.photos,
            permissionButtonText = locationButtonText,
            onPermissionButtonClick = {
                if (finePermission.status.shouldShowRationale || coarsePermission.status.shouldShowRationale) {
                    permissionsState.launchMultiplePermissionRequest()
                } else {
                    openAppSettings(context)
                }
            },
            onStartRoute = { locationViewModel.startRoute() },
            onStopRoute = { locationViewModel.stopRoute() },
            onClearRoute = {
                locationViewModel.clearRoute()
                photosViewModel.clearPhotos()
            },
            modifier = Modifier
                .weight(if (isPortrait) 1.5f else 2f)
                .fillMaxHeight()
        )
        CameraRegion(
            isPermissionGranted = isCameraGranted,
            photos = photosState.photos,
            permissionButtonText = cameraButtonText,
            onPermissionButtonClick = {
                if (cameraPermission.status.shouldShowRationale) {
                    permissionsState.launchMultiplePermissionRequest()
                } else {
                    openAppSettings(context)
                }
            },
            onPhotoSaved = { name, uri ->
                photosViewModel.addPhoto(name, uri, locationState.currentLocation)
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}
