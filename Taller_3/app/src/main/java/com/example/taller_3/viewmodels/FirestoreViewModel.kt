package com.example.taller_3.viewmodels

import androidx.lifecycle.ViewModel
import com.example.taller_3.R
import com.example.taller_3.data.UserInfo
import com.example.taller_3.repository.FirestoreUserRepository
import com.example.taller_3.repository.Subscription
import com.example.taller_3.repository.UserRepository
import com.example.taller_3.utils.UiText
import com.example.taller_3.utils.orResource
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

data class FirestoreState(
    val user: UserInfo? = null,
    val onlineUsers: Map<String, UserInfo> = emptyMap(),
    val loading: Boolean = false,
)

sealed interface FirestoreEvent {
    data class Error(val message: UiText) : FirestoreEvent
}

class FirestoreViewModel(
    private val userRepo: UserRepository = FirestoreUserRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(FirestoreState())
    val state: StateFlow<FirestoreState> = _state.asStateFlow()

    private val _events = Channel<FirestoreEvent>(Channel.BUFFERED)
    val events: Flow<FirestoreEvent> = _events.receiveAsFlow()

    private var userListener: Subscription? = null
    private var onlineUsersListener: Subscription? = null

    fun saveUser(uid: String, info: UserInfo, onDone: (Boolean) -> Unit = {}) {
        _state.update { it.copy(loading = true) }
        userRepo.save(uid, info) { result ->
            _state.update { it.copy(loading = false) }
            result.onFailure(::emitError)
            onDone(result.isSuccess)
        }
    }

    fun loadUser(uid: String) {
        _state.update { it.copy(loading = true) }
        userRepo.get(uid) { result ->
            _state.update { it.copy(loading = false) }
            result.onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure(::emitError)
        }
    }

    fun observeUser(uid: String) {
        userListener?.cancel()
        userListener = userRepo.observe(uid) { result ->
            result.onSuccess { user -> _state.update { it.copy(user = user) } }
                .onFailure(::emitError)
        }
    }

    fun updateUser(uid: String, fields: Map<String, Any?>, onDone: (Boolean) -> Unit = {}) {
        userRepo.update(uid, fields) { result ->
            result.onFailure(::emitError)
            onDone(result.isSuccess)
        }
    }

    fun deleteUser(uid: String, onDone: (Boolean) -> Unit = {}) {
        userRepo.delete(uid) { result ->
            result.onSuccess { _state.update { it.copy(user = null) } }
                .onFailure(::emitError)
            onDone(result.isSuccess)
        }
    }

    // Marca al usuario en línea y empieza un recorrido nuevo
    fun goOnline(uid: String) {
        updateUser(
            uid,
            mapOf(
                "online" to true,
                "route" to emptyList<Any>(),
                "lastSeen" to System.currentTimeMillis(),
            ),
        )
    }

    // Marca al usuario fuera de línea y borra su recorrido y su posición
    fun goOffline(uid: String, onDone: (Boolean) -> Unit = {}) {
        updateUser(
            uid,
            mapOf(
                "online" to false,
                "route" to emptyList<Any>(),
                "latitude" to null,
                "longitude" to null,
            ),
            onDone,
        )
    }

    // Guarda un cambio de posición del usuario y lo agrega a su recorrido
    fun updateLocation(uid: String, latitude: Double, longitude: Double) {
        userRepo.updateLocation(uid, latitude, longitude) { result ->
            result.onFailure(::emitError)
        }
    }

    fun observeOnlineUsers() {
        onlineUsersListener?.cancel()
        onlineUsersListener = userRepo.observeOnlineUsers { result ->
            result.onSuccess { users -> _state.update { it.copy(onlineUsers = users) } }
                .onFailure(::emitError)
        }
    }

    fun stopObservingOnlineUsers() {
        onlineUsersListener?.cancel()
        onlineUsersListener = null
        _state.update { it.copy(onlineUsers = emptyMap()) }
    }

    fun stopObserving() {
        userListener?.cancel()
        userListener = null
    }

    override fun onCleared() {
        stopObserving()
        onlineUsersListener?.cancel()
        // Si se cierra la app sin apagar la localizacion, el usuario no debe quedar en linea para los demas
        FirebaseAuth.getInstance().currentUser?.uid?.let { goOffline(it) }
    }

    private fun emitError(e: Throwable) {
        _events.trySend(FirestoreEvent.Error(e.localizedMessage.orResource(R.string.firestore_view_model_unknown_error)))
    }
}
