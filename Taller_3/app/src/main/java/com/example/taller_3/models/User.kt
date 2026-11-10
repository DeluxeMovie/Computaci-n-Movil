package com.example.taller_3.models

import com.google.firebase.firestore.GeoPoint

/**
 * Datos secundarios del usuario que se guardan en Firestore (users/{uid}).
 * El email y el password los guarda Firebase Authentication.
 * La foto de perfil vive en Storage; aquí solo se guarda su URL.
 * Los valores por defecto son necesarios para que Firebase pueda deserializar la clase.
 *
 * @property route puntos del recorrido mientras el usuario tiene la localización encendida.
 * @property lastSeen última vez (en milisegundos) que el usuario reportó que seguía en línea.
 */
data class User(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val photoUrl: String? = null,
    val online: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val route: List<GeoPoint> = emptyList(),
    val lastSeen: Long = 0L,
)
