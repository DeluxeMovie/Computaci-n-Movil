package com.example.taller_3.models

/**
 * Datos secundarios del usuario que se guardan en Realtime Database (users/{uid}).
 * El email y el password los guarda Firebase Authentication.
 * Los valores por defecto son necesarios para que Firebase pueda deserializar la clase.
 */
data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val online: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
