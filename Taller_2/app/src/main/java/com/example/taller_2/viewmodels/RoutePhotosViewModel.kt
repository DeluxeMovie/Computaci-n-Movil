package com.example.taller_2.viewmodels

import android.location.Location
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.taller_2.data.RoutePhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RoutePhotosState(
    val photos: List<RoutePhoto> = emptyList()
)

class RoutePhotosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RoutePhotosState())
    val uiState: StateFlow<RoutePhotosState> = _uiState.asStateFlow()

    /**
     * Agrega una foto a la lista del recorrido, junto con la ubicación donde fue tomada.
     */
    fun addPhoto(name: String, uri: Uri, location: Location?) {
        val photo = RoutePhoto(
            name = name,
            uri = uri,
            latitude = location?.latitude,
            longitude = location?.longitude
        )
        _uiState.update { it.copy(photos = it.photos.plus(photo)) }
    }

    /**
     * Quita todas las fotos de la lista del recorrido. No las borra de la galería.
     */
    fun clearPhotos() {
        _uiState.update { it.copy(photos = emptyList()) }
    }
}
