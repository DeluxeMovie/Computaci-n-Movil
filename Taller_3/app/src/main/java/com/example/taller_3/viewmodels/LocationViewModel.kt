package com.example.taller_3.viewmodels

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class LocationState(
    val isRecording: Boolean = false,
    val route: List<Location> = emptyList()
)

class LocationViewModel : ViewModel() {
    private val interval = 5000L // 5 seconds
    private val minDistance = 5F // 5 meters
    private val fastestInterval = 250L

    private val _uiState = MutableStateFlow(LocationState())
    val state = _uiState.asStateFlow()

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }

    fun setup(context: Context) {
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
                    currentState.copy(route = currentState.route.plus(locationResult.locations))
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        // setup() solo se llama cuando el permiso ya fue concedido
        if (!::fusedLocationClient.isInitialized) return
        _uiState.update {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            it.copy(isRecording = true)
        }
    }

    fun stopLocationUpdates() {
        if (!::fusedLocationClient.isInitialized) return
        _uiState.update {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            it.copy(isRecording = false)
        }
    }

    fun clearRoute() {
        _uiState.update {
            it.copy(route = emptyList())
        }
    }
}
