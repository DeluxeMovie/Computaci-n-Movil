package com.example.taller_3.ui.components

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/** Foto de perfil con un botón para cambiarla. Si no hay foto muestra la inicial del nombre. */
@Composable
fun ProfilePhoto(
    imageUrl: String?,
    name: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onChangePhoto: () -> Unit = {},
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
        TextButton(onClick = onChangePhoto, enabled = enabled) {
            Text(stringResource(R.string.change_photo))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePhotoPreview() {
    MaterialTheme { ProfilePhoto(imageUrl = null, name = "Jane Doe") }
}
