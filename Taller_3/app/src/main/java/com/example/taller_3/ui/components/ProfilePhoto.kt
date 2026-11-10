package com.example.taller_3.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.example.taller_3.R

/**
 * Foto de perfil con un botón para cambiarla. Si no hay foto muestra la inicial del nombre.
 * Hay dos formas de cambiarla: tomar una foto con la cámara (pide su permiso y abre la vista previa)
 * o elegir una imagen de la galería (no necesita permisos).
 *
 * @param onPhotoSelected se llama con el Uri de la foto tomada o elegida.
 */
@Composable
fun ProfilePhoto(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onPhotoSelected: (Uri) -> Unit = {},
) {
    val context = LocalContext.current
    var showCamera by rememberSaveable { mutableStateOf(false) }
    var cameraDenied by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraDenied = !granted
        showCamera = granted
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onPhotoSelected(uri)
    }

    fun openCamera() {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) showCamera = true
        else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // La vista de la cámara sigue activa aunque se deshabilite, por eso se cierra
    LaunchedEffect(enabled) {
        if (!enabled) showCamera = false
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showCamera) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                CameraPreviewContent(
                    modifier = Modifier.fillMaxSize(),
                    onPhotoTaken = { uri ->
                        showCamera = false
                        onPhotoSelected(uri)
                    },
                )
                IconButton(
                    onClick = { showCamera = false },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.close_camera),
                    )
                }
            }
        } else {
            val photoModifier = Modifier
                .size(120.dp)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clip(CircleShape)

            if (imageUrl.isNullOrBlank()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = photoModifier.background(MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Text(
                        text = name.trim().firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(R.string.profile_picture_of, name),
                    contentScale = ContentScale.Crop,
                    modifier = photoModifier,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { openCamera() }, enabled = enabled) {
                    Text(stringResource(R.string.take_photo))
                }
                TextButton(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    enabled = enabled,
                ) {
                    Text(stringResource(R.string.choose_from_gallery))
                }
            }
        }
        if (cameraDenied) {
            Text(
                text = stringResource(R.string.camera_permission_denied),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePhotoPreview() {
    MaterialTheme { ProfilePhoto(imageUrl = null, name = "Jane Doe") }
}
