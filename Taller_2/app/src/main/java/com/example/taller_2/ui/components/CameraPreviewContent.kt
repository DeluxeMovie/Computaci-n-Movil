package com.example.taller_2.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_2.R
import com.example.taller_2.viewmodels.CameraPreviewViewModel

/**
 * Vista previa de la cámara con un botón para cambiar entre cámara frontal/trasera
 * y un botón para tomar fotos.
 *
 * @param onPhotoSaved se llama con el nombre y la ubicación de la foto cuando se guarda en la galería.
 */
@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    viewModel: CameraPreviewViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPhotoSaved: (name: String, uri: Uri) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Enlaza la cámara mientras este componente esté en pantalla
    LaunchedEffect(lifecycleOwner) {
        viewModel.bindToCamera(context.applicationContext, lifecycleOwner)
    }

    Box(modifier = modifier) {
        uiState.surfaceRequest?.let { request ->
            val coordinateTransformer = remember { MutableCoordinateTransformer() }
            CameraXViewfinder(
                surfaceRequest = request,
                coordinateTransformer = coordinateTransformer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { tapCoords ->
                            with(coordinateTransformer) {
                                viewModel.tapToFocus(tapCoords.transform())
                            }
                        }
                    }
            )
        }

        // Botón para cambiar entre la cámara frontal y la trasera
        FilledTonalIconButton(
            onClick = { viewModel.toggleCameraLens() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Cameraswitch,
                contentDescription = stringResource(R.string.switch_camera)
            )
        }

        // Botón para tomar la foto
        FilledTonalIconButton(
            onClick = {
                viewModel.takePhoto(
                    context = context,
                    onPhotoSaved = onPhotoSaved,
                    onPhotoError = {
                        Toast.makeText(context, R.string.photo_error, Toast.LENGTH_SHORT).show()
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Camera,
                contentDescription = stringResource(R.string.take_photo)
            )
        }
    }
}
