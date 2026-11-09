package com.example.taller_3.viewmodels

import androidx.lifecycle.ViewModel
import com.example.taller_3.models.User
import com.example.taller_3.repository.UserRepository
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface ProfileState {
    data object Idle : ProfileState
    data object Loading : ProfileState
    data class Error(val message: String) : ProfileState
}

sealed interface ProfileEvent {
    data object ProfileUpdated : ProfileEvent
    data object PasswordChanged : ProfileEvent
}

class ProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _state = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    private var observedUid: String? = null

    private val userListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            _user.value = snapshot.getValue(User::class.java)
        }

        override fun onCancelled(error: DatabaseError) {
            _state.value = ProfileState.Error(error.message)
        }
    }

    // Se llama al entrar a la pantalla, así siempre se observa al usuario que tiene la sesión activa
    fun startObserving() {
        val uid = auth.currentUser?.uid ?: return
        stopObserving()
        _user.value = null
        observedUid = uid
        userRepository.observeUser(uid, userListener)
    }

    fun stopObserving() {
        observedUid?.let { userRepository.stopObservingUser(it, userListener) }
        observedUid = null
    }

    override fun onCleared() {
        stopObserving()
    }

    fun updateProfile(name: String, phone: String) {
        val uid = auth.currentUser?.uid ?: return
        if (name.isBlank() || phone.isBlank()) {
            _state.value = ProfileState.Error("Name and phone number are required")
            return
        }
        _state.value = ProfileState.Loading
        userRepository.updateProfile(uid, name.trim(), phone.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = ProfileState.Idle
                    _events.trySend(ProfileEvent.ProfileUpdated)
                } else {
                    _state.value = ProfileState.Error(task.exception?.localizedMessage ?: "Unknown error")
                }
            }
    }

    // Firebase pide una sesión reciente para cambiar la contraseña, por eso se reautentica primero
    fun changePassword(currentPassword: String, newPassword: String) {
        val firebaseUser = auth.currentUser
        val email = firebaseUser?.email
        if (firebaseUser == null || email == null) return
        if (currentPassword.isBlank() || newPassword.isBlank()) {
            _state.value = ProfileState.Error("Current and new password are required")
            return
        }
        _state.value = ProfileState.Loading
        firebaseUser.reauthenticate(EmailAuthProvider.getCredential(email, currentPassword))
            .onSuccessTask { firebaseUser.updatePassword(newPassword) }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = ProfileState.Idle
                    _events.trySend(ProfileEvent.PasswordChanged)
                } else {
                    _state.value = ProfileState.Error(task.exception?.localizedMessage ?: "Unknown error")
                }
            }
    }

    fun clearError() {
        if (_state.value is ProfileState.Error) _state.value = ProfileState.Idle
    }
}
