package com.example.taller_2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller_2.R
import com.example.taller_2.data.RoutePhoto

/**
 * Lista horizontal con las fotos tomadas durante el recorrido.
 * Cuando se agrega una foto, la lista se desplaza hasta ella.
 */
@Composable
fun PhotoListComponent(
    photos: List<RoutePhoto>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(photos.size) {
        if (photos.isNotEmpty()) {
            listState.animateScrollToItem(photos.lastIndex)
        }
    }

    if (photos.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.photos_empty),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    } else {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(items = photos, key = { it.uri.toString() }) { photo ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    AsyncImage(
                        model = photo.uri,
                        contentDescription = stringResource(R.string.photo_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}
