package com.example.taller_3.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import com.example.taller_3.models.OnlineUser
import com.example.taller_3.repository.UserRepository
import com.example.taller_3.utils.UiText
import com.example.taller_3.utils.toUiText
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapState(
    val isSharing: Boolean = false,
    val currentLocation: Location? = null,
    // Recorrido del usuario mientras tiene la localización encendida
    val route: List<GeoPoint> = emptyList(),
    val onlineUsers: List<OnlineUser> = emptyList(),
    val error: UiText? = null,
)

class MapViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {
    private val interval = 5000L // 5 segundos
    private val minDistance = 5F // 5 metros
    private val heartbeat = 60_000L // 1 minuto
    private val staleAfter = 3 * 60_000L // 3 minutos

    private val _state = MutableStateFlow(MapState())
    val state: StateFlow<MapState> = _state.asStateFlow()

    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var usersRegistration: ListenerRegistration? = null

    // Última posición enviada a la base de datos y cuándo se envió
    private var lastSentLocation: Location? = null
    private var lastSentTime = 0L

    // Se llama al entrar a la pantalla del mapa y se detiene al salir
    fun startObservingUsers() {
        stopObservingUsers()
        usersRegistration = userRepository.observeOnlineUsers(
            onChange = { onlineUsers ->
                val myUid = auth.currentUser?.uid
                val now = System.currentTimeMillis()
                val others = onlineUsers.filter { (uid, user) ->
                    // Se descarta a quien cerró la app sin apagar la localización (dejó de reportarse)
                    uid != myUid && user.latitude != null && user.longitude != null &&
                            now - user.lastSeen <= staleAfter
                }
                _state.update { it.copy(onlineUsers = others) }
            },
            onError = { e -> _state.update { it.copy(error = e.toUiText()) } },
        )
    }

    fun stopObservingUsers() {
        usersRegistration?.remove()
        usersRegistration = null
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

        lastSentLocation = null
        lastSentTime = System.currentTimeMillis()
        userRepository.startSharing(uid).addOnFailureListener(::reportError)

        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, interval).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { onNewLocation(uid, it) }
            }
        }
        fusedLocationClient = client
        locationCallback = callback

        // Usa la última ubicación conocida mientras llega la primera actualización
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null && _state.value.currentLocation == null) {
                _state.update { it.copy(currentLocation = location) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        _state.update { it.copy(isSharing = true, route = emptyList()) }
    }

    // Solo se registra un cambio de posición si el usuario se movió lo suficiente;
    // si está quieto igual se avisa cada cierto tiempo que sigue en línea
    private fun onNewLocation(uid: String, location: Location) {
        val now = System.currentTimeMillis()
        val last = lastSentLocation
        if (last == null || location.distanceTo(last) >= minDistance) {
            lastSentLocation = location
            lastSentTime = now
            _state.update {
                it.copy(
                    currentLocation = location,
                    route = it.route + GeoPoint(location.latitude, location.longitude),
                )
            }
            userRepository.updateLocation(uid, location.latitude, location.longitude)
                .addOnFailureListener(::reportError)
        } else if (now - lastSentTime >= heartbeat) {
            lastSentTime = now
            userRepository.keepAlive(uid).addOnFailureListener(::reportError)
        }
    }

    /**
     * Deja de escuchar la ubicación, borra el recorrido y marca al usuario como fuera de línea.
     * [onComplete] se llama cuando la base de datos confirma el cambio, útil para cerrar sesión después.
     */
    fun stopSharing(onComplete: () -> Unit = {}) {
        stopLocationUpdates()
        _state.update { it.copy(isSharing = false, currentLocation = null, route = emptyList()) }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete()
            return
        }
        userRepository.setOffline(uid).addOnCompleteListener { onComplete() }
    }

    private fun stopLocationUpdates() {
        val callback = locationCallback
        if (callback != null) fusedLocationClient?.removeLocationUpdates(callback)
        locationCallback = null
        fusedLocationClient = null
    }

    private fun reportError(e: Exception) {
        _state.update { it.copy(error = e.toUiText()) }
    }

    fun showError(message: UiText) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        stopLocationUpdates()
        usersRegistration?.remove()
    }
}
