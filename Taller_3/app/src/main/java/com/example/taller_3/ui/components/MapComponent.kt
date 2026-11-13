package com.example.taller_3.ui.components

import android.location.Location
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.taller_3.data.RoutePoint
import com.example.taller_3.data.UserInfo
import com.example.taller_3.utils.MarkerBitmapUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.ImageHolder
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.IconImage
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotation
import com.mapbox.maps.extension.compose.annotation.generated.PolylineAnnotationGroup
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.standard.MapboxStandardStyle
import com.mapbox.maps.extension.compose.style.standard.rememberStandardStyleState
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.LineCap
import com.mapbox.maps.extension.style.layers.properties.generated.LineJoin
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SELF_COLOR = android.graphics.Color.rgb(30, 136, 229)
private val OTHER_COLOR = android.graphics.Color.rgb(229, 57, 53)

/**
 * Mapa de Mapbox que muestra la posición y el recorrido del usuario autenticado y los de los
 * demás usuarios en línea. Cada usuario se muestra con su foto de perfil.
 *
 * @param isRecording indica si el usuario tiene la localización encendida. Mientras sea cierto
 * se muestra su posición (puck) con su foto y la cámara lo sigue.
 * @param route recorrido del usuario autenticado, se dibuja como una línea.
 * @param myName nombre del usuario autenticado.
 * @param myPhotoUrl foto de perfil del usuario autenticado.
 * @param onlineUsers otros usuarios en línea, cada uno con su marcador y su recorrido.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    defaultZoom: Double = 16.0,
    defaultPitch: Double = 45.0,
    isRecording: Boolean = false,
    route: List<Location> = emptyList(),
    myName: String = "",
    myPhotoUrl: String? = null,
    onlineUsers: Map<String, UserInfo> = emptyMap(),
) {
    val context = LocalContext.current

    // Map viewport state: empieza en Bogotá mientras llega la ubicación del usuario
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(defaultZoom)
            center(Point.fromLngLat(-74.0641630119028, 4.627389316647156))
            pitch(defaultPitch)
        }
    }
    // Map style setup: los objetos 3D hacen que se noten los edificios al inclinar la cámara
    val standardStyleState = rememberStandardStyleState {
        configurationsState.apply {
            showPedestrianRoads = BooleanValue(true)
            show3dObjects = BooleanValue(true)
            showPlaceLabels = BooleanValue(false)
            showPointOfInterestLabels = BooleanValue(false)
            showRoadLabels = BooleanValue(true)
            showIndoor = BooleanValue(true)
        }
    }
    val followPuckOptions = remember {
        FollowPuckViewportStateOptions.Builder().zoom(defaultZoom).pitch(defaultPitch).build()
    }

    // La posición del usuario se muestra con su foto de perfil; mientras se descarga, con su inicial
    val myInitial = remember(myName) { MarkerBitmapUtils.createUserMarker(myName, SELF_COLOR) }
    val myMarker by produceState(initialValue = myInitial, myPhotoUrl, myInitial) {
        value = myInitial
        if (!myPhotoUrl.isNullOrBlank()) {
            val photo = withContext(Dispatchers.IO) { MarkerBitmapUtils.loadPhoto(context, myPhotoUrl) }
            if (photo != null) value = MarkerBitmapUtils.createPhotoMarker(photo, SELF_COLOR)
        }
    }

    MapboxMap(
        modifier = modifier.fillMaxSize(),
        mapViewportState = mapViewportState,
        scaleBar = { },
        style = {
            MapboxStandardStyle(standardStyleState = standardStyleState)
        },
    ) {
        // Enable user position puck: solo mientras la localización está encendida (ya hay permiso)
        MapEffect(isRecording, myMarker) { mapView ->
            mapView.location.updateSettings {
                pulsingEnabled = true
                locationPuck = LocationPuck2D(topImage = ImageHolder.from(myMarker))
                enabled = isRecording
            }
            if (isRecording) mapViewportState.transitionToFollowPuckState(followPuckOptions)
        }
        // Polyline
        RouteLine(
            points = route.map { Point.fromLngLat(it.longitude, it.latitude) },
            color = SELF_COLOR,
        )
        // Otros usuarios en línea
        onlineUsers.forEach { (uid, user) ->
            val latitude = user.latitude
            val longitude = user.longitude
            if (latitude != null && longitude != null) {
                key(uid) {
                    RouteLine(points = user.route.map(RoutePoint::toPoint), color = OTHER_COLOR)
                    UserMarker(
                        name = user.displayName,
                        photoUrl = user.photo,
                        point = Point.fromLngLat(longitude, latitude),
                        color = OTHER_COLOR,
                    )
                }
            }
        }
    }
}

private fun RoutePoint.toPoint(): Point = Point.fromLngLat(longitude, latitude)

/**
 * Línea del recorrido de un usuario, se dibuja cuando hay al menos dos puntos.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun RouteLine(points: List<Point>, color: Int) {
    if (points.size < 2) return
    PolylineAnnotationGroup(
        annotations = listOf(PolylineAnnotationOptions().withPoints(points))
    ) {
        lineCap = LineCap.ROUND
        lineWidth = 10.0
        lineColor = Color(color)
        lineBorderWidth = 2.0
        lineBorderColor = Color.White
        lineJoin = LineJoin.ROUND
    }
}

/**
 * Marcador del mapa con la foto de perfil del usuario y su nombre como título.
 * Mientras la foto se descarga, o si no tiene, muestra su inicial.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun UserMarker(name: String, photoUrl: String?, point: Point, color: Int) {
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

    PointAnnotation(point = point) {
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
