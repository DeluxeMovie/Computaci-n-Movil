package com.example.taller_3.ui.components

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.taller_3.R
import com.example.taller_3.models.OnlineUser
import com.example.taller_3.utils.MarkerBitmapUtils
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEFAULT_ZOOM = 16.0
// Inclinación de la cámara, permite ver los edificios y superficies en 3D
private const val MAP_PITCH = 60.0
private val SELF_COLOR = android.graphics.Color.rgb(30, 136, 229)
private val OTHER_COLOR = android.graphics.Color.rgb(229, 57, 53)

/**
 * Mapa de Mapbox que muestra la posición y el recorrido del usuario autenticado y los de los
 * demás usuarios en línea. Cada marcador usa la foto de perfil de su usuario.
 *
 * @param currentLocation ubicación del usuario, es null cuando tiene la localización apagada.
 * @param route recorrido del usuario autenticado.
 * @param myName nombre del usuario autenticado.
 * @param myPhotoUrl foto de perfil del usuario autenticado.
 * @param onlineUsers otros usuarios en línea, cada uno con su marcador y su recorrido.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun UsersMap(
    currentLocation: Location?,
    route: List<GeoPoint>,
    myName: String,
    myPhotoUrl: String?,
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

    val youLabel = stringResource(R.string.you)
    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = { },
        style = { MapboxStandardStyle() },
    ) {
        RouteLine(points = route, color = SELF_COLOR)
        if (currentLocation != null) {
            UserMarker(
                name = myName.ifBlank { youLabel },
                photoUrl = myPhotoUrl,
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
                    RouteLine(points = onlineUser.user.route, color = OTHER_COLOR)
                    UserMarker(
                        name = onlineUser.user.name,
                        photoUrl = onlineUser.user.photoUrl,
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
 * Línea del recorrido de un usuario, se dibuja cuando hay al menos dos puntos.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun RouteLine(points: List<GeoPoint>, color: Int) {
    if (points.size < 2) return
    PolylineAnnotationGroup(
        annotations = listOf(
            PolylineAnnotationOptions().withPoints(
                points.map { Point.fromLngLat(it.longitude, it.latitude) }
            )
        )
    ) {
        this.lineCap = LineCap.ROUND
        this.lineJoin = LineJoin.ROUND
        this.lineWidth = 8.0
        this.lineColor = Color(color)
        this.lineBorderWidth = 2.0
        this.lineBorderColor = Color.White
    }
}

/**
 * Marcador del mapa con la foto de perfil del usuario y su nombre como título.
 * Mientras la foto se descarga, o si no tiene, muestra su inicial.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun UserMarker(
    name: String,
    photoUrl: String?,
    latitude: Double,
    longitude: Double,
    color: Int,
) {
    val context = LocalContext.current
    val initialMarker = remember(name, color) { MarkerBitmapUtils.createUserMarker(name, color) }

    // La foto se descarga en un hilo secundario para no bloquear la interfaz
    val bitmap by produceState(initialValue = initialMarker, photoUrl, initialMarker) {
        value = initialMarker
        if (!photoUrl.isNullOrBlank()) {
            val photo = withContext(Dispatchers.IO) { MarkerBitmapUtils.loadPhoto(context, photoUrl) }
            if (photo != null) value = MarkerBitmapUtils.createPhotoMarker(photo, color)
        }
    }

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
