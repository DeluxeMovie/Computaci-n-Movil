package com.example.taller_3.ui.screens

import android.Manifest
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.ui.components.ErrorMessage
import com.example.taller_3.ui.components.LocationSwitch
import com.example.taller_3.ui.components.MapComponent
import com.example.taller_3.ui.components.NoPermissionCard
import com.example.taller_3.ui.components.RequestPermissionCard
import com.example.taller_3.utils.UiText
import com.example.taller_3.viewmodels.AuthViewModel
import com.example.taller_3.viewmodels.FirestoreEvent
import com.example.taller_3.viewmodels.FirestoreViewModel
import com.example.taller_3.viewmodels.LocationViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.delay

// Cada cuánto un usuario quieto avisa que sigue en línea, y cuánto se espera antes de ocultarlo
private const val HEARTBEAT_MILLIS = 60_000L
private const val STALE_MILLIS = 3 * 60_000L
private const val STALE_CHECK_MILLIS = 30_000L

/**
 * Pantalla principal: un mapa con la posición y el recorrido del usuario y los de los demás
 * usuarios en línea, y un switch para encender o apagar la localización.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = viewModel(),
    firestoreViewModel: FirestoreViewModel = viewModel(),
    locationViewModel: LocationViewModel = viewModel(),
) {
    val context = LocalContext.current
    val locationPermission =
        rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    val currentUser by authViewModel.currentUser.collectAsState()
    val firestoreState by firestoreViewModel.state.collectAsState()
    val locationState by locationViewModel.state.collectAsState()

    val uid = currentUser?.uid
    var error by remember { mutableStateOf<UiText?>(null) }

    // launch the permission request when the composable is first composed
    LaunchedEffect(Unit) {
        locationPermission.launchPermissionRequest()
    }
    // set up the locationViewModel when the permission is granted
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted)
            locationViewModel.setup(context)
    }

    // Los usuarios en línea se escuchan solo mientras esta pantalla está visible
    DisposableEffect(firestoreViewModel) {
        firestoreViewModel.observeOnlineUsers()
        onDispose { firestoreViewModel.stopObservingOnlineUsers() }
    }

    LaunchedEffect(firestoreViewModel) {
        firestoreViewModel.events.collect { event ->
            if (event is FirestoreEvent.Error) error = event.message
        }
    }

    // Cada cambio de posición se registra en la base de datos
    val currentLocation = locationState.route.lastOrNull()
    LaunchedEffect(currentLocation) {
        if (uid != null && currentLocation != null && locationState.isRecording) {
            firestoreViewModel.updateLocation(uid, currentLocation.latitude, currentLocation.longitude)
        }
    }

    // Aunque el usuario no se mueva, avisa cada cierto tiempo que sigue en línea
    LaunchedEffect(locationState.isRecording, uid) {
        if (uid == null || !locationState.isRecording) return@LaunchedEffect
        while (true) {
            delay(HEARTBEAT_MILLIS)
            firestoreViewModel.updateUser(uid, mapOf("lastSeen" to System.currentTimeMillis()))
        }
    }

    // Solo se muestran los demás usuarios con posición que siguen reportándose
    val now by produceState(System.currentTimeMillis()) {
        while (true) {
            delay(STALE_CHECK_MILLIS)
            value = System.currentTimeMillis()
        }
    }
    val otherUsers = remember(firestoreState.onlineUsers, uid, now) {
        firestoreState.onlineUsers.filter { (id, user) ->
            id != uid && user.latitude != null && user.longitude != null &&
                    now - user.lastSeen <= STALE_MILLIS
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        MapComponent(
            isRecording = locationState.isRecording,
            route = locationState.route,
            myName = firestoreState.user?.displayName.orEmpty(),
            myPhotoUrl = firestoreState.user?.photo,
            onlineUsers = otherUsers,
        )
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 16.dp),
        ) {
            if (!locationPermission.status.isGranted) {
                if (locationPermission.status.shouldShowRationale) {
                    RequestPermissionCard(onRequestPermission = {
                        locationPermission.launchPermissionRequest()
                    })
                } else {
                    NoPermissionCard()
                }
            }
            ErrorMessage(
                message = error?.asString(),
                onDismiss = { error = null },
            )
        }
        LocationSwitch(
            checked = locationState.isRecording,
            enabled = locationPermission.status.isGranted,
            onCheckedChange = { enabled ->
                if (enabled) {
                    locationViewModel.startLocationUpdates()
                    if (uid != null) firestoreViewModel.goOnline(uid)
                } else {
                    // Al apagar la localización se borra la línea recorrida y el usuario queda desconectado
                    locationViewModel.stopLocationUpdates()
                    locationViewModel.clearRoute()
                    if (uid != null) firestoreViewModel.goOffline(uid)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        )
    }
}
