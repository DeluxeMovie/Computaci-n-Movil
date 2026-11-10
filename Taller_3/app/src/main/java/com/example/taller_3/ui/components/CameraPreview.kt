package com.example.taller_3.ui.components

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
import androidx.compose.material3.FilledIconButton
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
import com.example.taller_3.R
import com.example.taller_3.viewmodels.CameraViewModel

/**
 * Vista previa de la cámara con un botón para cambiar de lente y otro para tomar la foto.
 * Al tocar la vista previa la cámara enfoca en ese punto.
 *
 * @param onPhotoTaken se llama con el Uri de la foto una vez guardada.
 */
@Composable
fun CameraPreviewContent(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = viewModel(),
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onPhotoTaken: (Uri) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
        FilledTonalIconButton(
            onClick = { viewModel.toggleCameraLens() },
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(Icons.Default.Cameraswitch, contentDescription = stringResource(R.string.switch_camera))
        }
        FilledIconButton(
            onClick = {
                viewModel.takePhoto(context) { uri -> if (uri != null) onPhotoTaken(uri) }
                Toast.makeText(context, context.getString(R.string.taking_photo), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .size(64.dp)
        ) {
            Icon(
                Icons.Default.Camera,
                contentDescription = stringResource(R.string.take_photo),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
