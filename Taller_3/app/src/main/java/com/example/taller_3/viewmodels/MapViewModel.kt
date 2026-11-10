package com.example.taller_3.viewmodels

import com.example.taller_3.utils.toUiText
import com.example.taller_3.utils.UiText
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.taller_3.models.OnlineUser
import com.example.taller_3.models.User
import com.example.taller_3.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapState(
    val isSharing: Boolean = false,
    val currentLocation: Location? = null,
    val onlineUsers: List<OnlineUser> = emptyList(),
    val error: UiText? = null,
)

class MapViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {
    private val interval = 5000L // 5 segundos
    private val minDistance = 5F // 5 metros
    private val fastestInterval = 1000L // 1 segundo

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    private val onlineUsersListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val myUid = auth.currentUser?.uid
            val users = snapshot.children.mapNotNull { child ->
                val uid = child.key ?: return@mapNotNull null
                val user = child.getValue(User::class.java) ?: return@mapNotNull null
                // Solo se muestran los demás usuarios que ya tienen una ubicación guardada
                if (uid == myUid || user.latitude == null || user.longitude == null) null
                else OnlineUser(uid, user)
            }
            _state.update { it.copy(onlineUsers = users) }
        }

        override fun onCancelled(error: DatabaseError) {
            _state.update { it.copy(error = UiText.Dynamic(error.message)) }
        }
    }

    // Se llama al entrar a la pantalla del mapa y se detiene al salir
    fun startObservingUsers() {
        stopObservingUsers()
        userRepository.observeOnlineUsers(onlineUsersListener)
    }

    fun stopObservingUsers() {
        userRepository.stopObservingOnlineUsers(onlineUsersListener)
        _state.update { it.copy(onlineUsers = emptyList()) }
    }

    /**
     * Empieza a escuchar la ubicación del dispositivo y a guardarla en la base de datos.
     * Debe llamarse cuando el permiso de ubicación ya fue concedido.
     */
    @SuppressLint("MissingPermission")
    fun startSharing(context: Context) {
        val uid = auth.currentUser?.uid ?: return
        if (_state.value.isSharing) return

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval)
            .setMinUpdateDistanceMeters(minDistance)
            .setMinUpdateIntervalMillis(fastestInterval)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation ?: return
                _state.update { it.copy(currentLocation = location) }
                userRepository.updateLocation(uid, location.latitude, location.longitude)
                    .addOnFailureListener { e ->
                        _state.update { it.copy(error = e.toUiText()) }
                    }
            }
        }
        fusedLocationClient = client
        locationCallback = callback

        userRepository.setOfflineOnDisconnect(uid)
        // Usa la última ubicación conocida mientras llega la primera actualización
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null && _state.value.currentLocation == null) {
                _state.update { it.copy(currentLocation = location) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        _state.update { it.copy(isSharing = true) }
    }

    /**
     * Deja de escuchar la ubicación y marca al usuario como fuera de línea.
     * [onComplete] se llama cuando la base de datos confirma el cambio, útil para cerrar sesión después.
     */
    fun stopSharing(onComplete: () -> Unit = {}) {
        val callback = locationCallback
        if (callback != null) fusedLocationClient?.removeLocationUpdates(callback)
        locationCallback = null
        fusedLocationClient = null
        _state.update { it.copy(isSharing = false, currentLocation = null) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete()
            return
        }
        userRepository.setOffline(uid).addOnCompleteListener { onComplete() }
    }

    fun showError(message: UiText) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        val callback = locationCallback
        if (callback != null) fusedLocationClient?.removeLocationUpdates(callback)
        userRepository.stopObservingOnlineUsers(onlineUsersListener)
    }
}
