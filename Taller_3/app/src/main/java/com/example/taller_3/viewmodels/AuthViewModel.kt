package com.example.taller_3.viewmodels

import androidx.lifecycle.ViewModel
import com.example.taller_3.models.User
import com.example.taller_3.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: FirebaseUser) : AuthState
    data class PasswordResetSent(val email: String) : AuthState
    data class Error(val message: String) : AuthState
}

sealed interface AuthEvent {
    data object NavigateToHome : AuthEvent
    data object NavigateToLogin : AuthEvent
    data class ShowMessage(val text: String) : AuthEvent
}

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val userRepository: UserRepository = UserRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow<AuthState>(
        auth.currentUser?.let { AuthState.Authenticated(it) } ?: AuthState.Idle
    )
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { _currentUser.value = it.currentUser }

    init {
        auth.addAuthStateListener(authListener)
    }

    override fun onCleared() {
        auth.removeAuthStateListener(authListener)
    }

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun register(name: String, email: String, phone: String, password: String) {
        if (!validate(email, password)) return
        if (name.isBlank() || phone.isBlank()) {
            _state.value = AuthState.Error("Name and phone number are required")
            return
        }
        _state.value = AuthState.Loading
        var createdUser: FirebaseUser? = null
        auth.createUserWithEmailAndPassword(email.trim(), password)
            .onSuccessTask { result ->
                val user = result.user!!
                createdUser = user
                // Online y ubicación quedan con sus valores por defecto, no se piden en el registro
                userRepository.saveUser(user.uid, User(name = name, email = email.trim(), phone = phone))
                    .continueWith { user }
            }
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _state.value = AuthState.Authenticated(task.result)
                    _events.trySend(AuthEvent.NavigateToHome)
                } else {
                    // Si falló guardar los datos, se elimina la cuenta para no dejarla incompleta
                    createdUser?.delete()
                    _state.value = AuthState.Error(task.exception?.localizedMessage ?: "Unknown error")
                }
            }
    }

    fun login(email: String, password: String) {
        if (!validate(email, password)) return
        _state.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email.trim(), password)
            .addOnCompleteListener { task ->
                _state.value = authResult(task.isSuccessful, if (task.isSuccessful) task.result?.user else null, task.exception)
                if (_state.value is AuthState.Authenticated) _events.trySend(AuthEvent.NavigateToHome)
            }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _state.value = AuthState.Error("Email is required")
            return
        }
        _state.value = AuthState.Loading
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                _state.value = if (task.isSuccessful) {
                    _events.trySend(AuthEvent.ShowMessage("Password reset email sent"))
                    AuthState.PasswordResetSent(email.trim())
                } else {
                    AuthState.Error(task.exception?.localizedMessage ?: "Unknown error")
                }
            }
    }

    fun logout() {
        auth.signOut()
        _state.value = AuthState.Idle
        _events.trySend(AuthEvent.NavigateToLogin)
    }

    fun clearError() {
        if (_state.value is AuthState.Error) _state.value = AuthState.Idle
    }

    private fun authResult(ok: Boolean, user: FirebaseUser?, e: Exception?): AuthState =
        if (ok && user != null) AuthState.Authenticated(user)
        else AuthState.Error(e?.localizedMessage ?: "Unknown error")

    private fun validate(email: String, password: String): Boolean {
        val error = when {
            email.isBlank() -> "Email is required"
            password.isBlank() -> "Password is required"
            else -> return true
        }
        _state.value = AuthState.Error(error)
        return false
    }
}
