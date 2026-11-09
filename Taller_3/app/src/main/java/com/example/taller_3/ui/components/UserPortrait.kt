package com.example.taller_3.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller_3.ui.theme.Taller_3Theme

@Composable
fun UserPortrait(
    imageUrl: String?,
    name: String,
    lastLogin: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (imageUrl.isNullOrBlank()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(84.dp)
                    .shadow(6.dp, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            ) {
                Text(
                    text = name.trim().firstOrNull()?.uppercase() ?: "?",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        } else {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Profile picture of $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(84.dp)
                    .shadow(6.dp, CircleShape)
                    .border(4.dp, Color.White, CircleShape)
                    .clip(CircleShape),
            )
        }
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "Last login: $lastLogin",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Light
                )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun UserPortraitPreview() {
    Taller_3Theme {
        UserPortrait(
            imageUrl = null,
            name = "Jane Doe",
            lastLogin = "Oct 4, 2026 10:55",
        )
    }
}
