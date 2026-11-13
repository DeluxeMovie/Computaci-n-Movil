package com.example.taller_3.data

/**
 * Datos del usuario que se guardan en Firestore (users/{uid}).
 * El email y el password los guarda Firebase Authentication.
 *
 * @property photo URL de la foto de perfil en Firebase Storage.
 * @property online indica si el usuario tiene la localización encendida.
 * @property route recorrido del usuario mientras tiene la localización encendida.
 * @property lastSeen última vez (en milisegundos) que el usuario reportó que seguía en línea.
 */
data class UserInfo(
    val displayName: String = "",
    val photo: String? = null,
    val phone: Long = 0L,
    val online: Boolean = false,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val route: List<RoutePoint> = emptyList(),
    val lastSeen: Long = 0L,
)
