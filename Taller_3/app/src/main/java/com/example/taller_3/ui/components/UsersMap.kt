package com.example.taller_3.ui.components

import com.example.taller_3.R
import androidx.compose.ui.res.stringResource
import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.taller_3.models.OnlineUser
import com.example.taller_3.utils.MarkerBitmapUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor

private const val DEFAULT_ZOOM = 16.0
// Inclinación de la cámara, permite ver los edificios y superficies en 3D
private const val MAP_PITCH = 60.0
private val SELF_COLOR = android.graphics.Color.rgb(30, 136, 229)
private val OTHER_COLOR = android.graphics.Color.rgb(229, 57, 53)

/**
 * Mapa de Mapbox que muestra la posición del usuario autenticado y la de los demás usuarios en línea.
 *
 * @param currentLocation ubicación del usuario, es null cuando tiene la localización apagada.
 * @param onlineUsers otros usuarios en línea, cada uno se muestra como un marcador.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun UsersMap(
    currentLocation: Location?,
    onlineUsers: List<OnlineUser>,
    modifier: Modifier = Modifier,
) {
    // Estado de la cámara del mapa. Empieza en Bogotá mientras llega la ubicación del usuario
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(DEFAULT_ZOOM)
            pitch(MAP_PITCH)
            center(Point.fromLngLat(-74.0641630119028, 4.627389316647156))
        }
    }

    // Cuando llega la primera ubicación, la cámara se mueve hacia el usuario
    val hasLocation = currentLocation != null
    LaunchedEffect(hasLocation) {
        if (currentLocation != null) {
            mapViewportState.flyTo(
                CameraOptions.Builder()
                    .center(Point.fromLngLat(currentLocation.longitude, currentLocation.latitude))
                    .zoom(DEFAULT_ZOOM)
                    .pitch(MAP_PITCH)
                    .build()
            )
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = { },
        style = { MapboxStandardStyle() },
    ) {
        if (currentLocation != null) {
            UserMarker(
                name = stringResource(R.string.you),
                latitude = currentLocation.latitude,
                longitude = currentLocation.longitude,
                color = SELF_COLOR,
            )
        }
        onlineUsers.forEach { onlineUser ->
            val latitude = onlineUser.user.latitude
            val longitude = onlineUser.user.longitude
            if (latitude != null && longitude != null) {
                key(onlineUser.uid) {
                    UserMarker(
                        name = onlineUser.user.name,
                        latitude = latitude,
                        longitude = longitude,
                        color = OTHER_COLOR,
                    )
                }
            }
        }
    }
}

/**
 * Marcador del mapa con la inicial del usuario y su nombre como título.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun UserMarker(name: String, latitude: Double, longitude: Double, color: Int) {
    val bitmap = remember(name, color) { MarkerBitmapUtils.createUserMarker(name, color) }

    PointAnnotation(point = Point.fromLngLat(longitude, latitude)) {
        iconImage = IconImage(bitmap)
        iconAnchor = IconAnchor.CENTER
        textField = name
        textAnchor = TextAnchor.TOP
        textOffset = listOf(0.0, 1.5)
        textSize = 12.0
        textColor = Color.Black
        textHaloColor = Color.White
        textHaloWidth = 2.0
    }
}
