package com.example.taller_2.ui.components

import android.graphics.Bitmap
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller_2.R
import com.example.taller_2.data.RoutePhoto
import com.example.taller_2.utils.MarkerBitmapUtils
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.ComposeMapInitOptions
import com.mapbox.maps.extension.compose.MapEffect
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
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Mapa de Mapbox que muestra la ubicación del usuario, el recorrido hecho
 * y un marcador con la foto en cada lugar donde se tomó una.
 *
 * @param route puntos del recorrido, se dibujan como una línea.
 * @param photos fotos tomadas, cada una con su ubicación se muestra como marcador.
 */
@OptIn(MapboxExperimental::class)
@Composable
fun MapComponent(
    modifier: Modifier = Modifier,
    defaultZoom: Double = 17.0,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    route: List<Location> = emptyList(),
    photos: List<RoutePhoto> = emptyList(),
) {
    // Estado de la cámara del mapa. Empieza en Bogotá mientras llega la ubicación del usuario
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            zoom(defaultZoom)
            center(Point.fromLngLat(-74.0641630119028, 4.627389316647156))
        }
    }
    // Se usa TextureView para que el mapa respete las esquinas redondeadas del contenedor
    val mapInitOptions = with(LocalDensity.current) {
        remember { ComposeMapInitOptions(density, textureView = true) }
    }
    val followPuckOptions = remember {
        FollowPuckViewportStateOptions.Builder().zoom(defaultZoom).pitch(0.0).build()
    }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier.fillMaxSize(),
            composeMapInitOptions = mapInitOptions,
            mapViewportState = mapViewportState,
            scaleBar = { },
            style = { MapboxStandardStyle() },
        ) {
            // Muestra la ubicación del usuario en el mapa y hace que la cámara la siga
            MapEffect(Unit) { mapView ->
                mapView.location.updateSettings {
                    locationPuck = createDefault2DPuck(withBearing = true)
                    puckBearing = PuckBearing.HEADING
                    puckBearingEnabled = true
                    pulsingEnabled = true
                    enabled = true
                }
                mapViewportState.transitionToFollowPuckState(followPuckOptions)
            }

            // Línea del recorrido, se dibuja cuando hay al menos dos puntos
            if (route.size >= 2) {
                PolylineAnnotationGroup(
                    annotations = listOf(
                        PolylineAnnotationOptions().withPoints(route.map {
                            Point.fromLngLat(it.longitude, it.latitude)
                        })
                    )
                ) {
                    this.lineCap = LineCap.ROUND
                    this.lineJoin = LineJoin.ROUND
                    this.lineWidth = 10.0
                    this.lineColor = lineColor
                    this.lineBorderWidth = 2.0
                    this.lineBorderColor = Color.White
                }
            }

            // Un marcador por cada foto que tiene ubicación
            photos.forEach { photo ->
                if (photo.latitude != null && photo.longitude != null) {
                    PhotoMarker(
                        photo = photo,
                        latitude = photo.latitude,
                        longitude = photo.longitude
                    )
                }
            }
        }

        // Centra la cámara en la ubicación del usuario y vuelve a seguirla
        FloatingActionButton(
            onClick = { mapViewportState.transitionToFollowPuckState(followPuckOptions) },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = stringResource(R.string.center_on_location)
            )
        }
    }
}

/**
 * Marcador del mapa que muestra una miniatura de la foto y tiene como título el nombre de la foto.
 */
@OptIn(MapboxExperimental::class)
@Composable
@MapboxMapComposable
private fun PhotoMarker(photo: RoutePhoto, latitude: Double, longitude: Double) {
    val context = LocalContext.current

    // Crea la miniatura en un hilo secundario para no bloquear la interfaz
    val markerBitmap by produceState<Bitmap?>(initialValue = null, photo.uri) {
        value = withContext(Dispatchers.IO) {
            MarkerBitmapUtils.createPhotoMarker(context, photo.uri)
        }
    }

    markerBitmap?.let { bitmap ->
        PointAnnotation(point = Point.fromLngLat(longitude, latitude)) {
            iconImage = IconImage(bitmap)
            iconAnchor = IconAnchor.BOTTOM
            textField = photo.name
            textAnchor = TextAnchor.TOP
            textSize = 12.0
            textColor = Color.Black
            textHaloColor = Color.White
            textHaloWidth = 2.0
        }
    }
}
