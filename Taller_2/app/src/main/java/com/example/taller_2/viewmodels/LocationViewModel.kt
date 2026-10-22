package com.example.taller_2.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationState(
    val isRecording: Boolean = false,
    val route: List<Location> = emptyList(),
    val currentLocation: Location? = null
)

class LocationViewModel : ViewModel() {
    private val interval = 5000L // 5 segundos
    private val minDistance = 5F // 5 metros
    private val fastestInterval = 1000L // 1 segundo

    private val _uiState = MutableStateFlow(LocationState())
    val state: StateFlow<LocationState> = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    /**
     * Configura el cliente de localización y empieza a escuchar la ubicación del usuario.
     * La ubicación actual se actualiza siempre; solo se agrega al recorrido si se está grabando.
     * Debe llamarse cuando el permiso de ubicación ya fue concedido.
     */
    @SuppressLint("MissingPermission")
    fun setup(context: Context) {
        // Evita dejar un callback anterior activo si se llama más de una vez
        stopLocationUpdates()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).apply {
            setMinUpdateDistanceMeters(minDistance)
            setMinUpdateIntervalMillis(fastestInterval)
            setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
            setWaitForAccurateLocation(true)
        }.build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                _uiState.update { currentState ->
                    currentState.copy(
                        currentLocation = locationResult.lastLocation,
                        route = if (currentState.isRecording) {
                            currentState.route.plus(locationResult.locations)
                        } else {
                            currentState.route
                        }
                    )
                }
            }
        }

        // Usa la última ubicación conocida mientras llega la primera actualización
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null && _uiState.value.currentLocation == null) {
                _uiState.update { it.copy(currentLocation = location) }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Deja de escuchar la ubicación del dispositivo.
     */
    private fun stopLocationUpdates() {
        if (::fusedLocationClient.isInitialized && ::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    /**
     * Empieza a guardar los puntos del recorrido.
     */
    fun startRoute() {
        _uiState.update { it.copy(isRecording = true) }
    }

    /**
     * Deja de guardar puntos, pero conserva el recorrido hecho hasta ahora.
     */
    fun stopRoute() {
        _uiState.update { it.copy(isRecording = false) }
    }

    /**
     * Borra todos los puntos del recorrido.
     */
    fun clearRoute() {
        _uiState.update { it.copy(route = emptyList()) }
    }
}
