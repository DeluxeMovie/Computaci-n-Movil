package com.example.taller_2.ui.components

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller_2.R
import com.example.taller_2.data.RoutePhoto

/**
 * Región de la cámara: muestra la vista previa y debajo la lista horizontal de fotos tomadas.
 * Si el permiso de la cámara no fue concedido, muestra un mensaje en su lugar.
 *
 * @param isPermissionGranted indica si el permiso de la cámara fue concedido.
 * @param permissionButtonText texto del botón del mensaje de permiso.
 * @param onPermissionButtonClick acción del botón del mensaje de permiso.
 * @param onPhotoSaved se llama cuando se toma y se guarda una foto.
 */
@Composable
fun CameraRegion(
    isPermissionGranted: Boolean,
    photos: List<RoutePhoto>,
    permissionButtonText: String,
    onPermissionButtonClick: () -> Unit,
    onPhotoSaved: (name: String, uri: Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (isPermissionGranted) {
            OutlinedCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CameraPreviewContent(
                    modifier = Modifier.fillMaxSize(),
                    onPhotoSaved = onPhotoSaved
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                PhotoListComponent(
                    photos = photos,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            OutlinedCard(modifier = Modifier.fillMaxSize()) {
                PermissionDeniedMessage(
                    message = stringResource(R.string.camera_permission_denied),
                    buttonText = permissionButtonText,
                    onButtonClick = onPermissionButtonClick
                )
            }
        }
    }
}
