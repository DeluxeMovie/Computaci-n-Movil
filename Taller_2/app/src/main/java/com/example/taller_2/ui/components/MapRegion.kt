package com.example.taller_2.ui.components

import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller_2.R
import com.example.taller_2.data.RoutePhoto

/**
 * Región del mapa: muestra el mapa con los botones del recorrido encima.
 * Si el permiso de ubicación no fue concedido, muestra un mensaje en su lugar.
 *
 * @param isPermissionGranted indica si el permiso de ubicación fue concedido.
 * @param isRecording indica si el recorrido se está grabando.
 * @param route puntos del recorrido.
 * @param photos fotos que se muestran como marcadores.
 * @param permissionButtonText texto del botón del mensaje de permiso.
 * @param onPermissionButtonClick acción del botón del mensaje de permiso.
 */
@Composable
fun MapRegion(
    isPermissionGranted: Boolean,
    isRecording: Boolean,
    route: List<Location>,
    photos: List<RoutePhoto>,
    permissionButtonText: String,
    onPermissionButtonClick: () -> Unit,
    onStartRoute: () -> Unit,
    onStopRoute: () -> Unit,
    onClearRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier) {
        if (isPermissionGranted) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapComponent(
                    modifier = Modifier.fillMaxSize(),
                    route = route,
                    photos = photos
                )
                RouteControls(
                    isRecording = isRecording,
                    routeSize = route.size,
                    onStartClick = onStartRoute,
                    onStopClick = onStopRoute,
                    onClearClick = onClearRoute,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
        } else {
            PermissionDeniedMessage(
                message = stringResource(R.string.location_permission_denied),
                buttonText = permissionButtonText,
                onButtonClick = onPermissionButtonClick
            )
        }
    }
}
