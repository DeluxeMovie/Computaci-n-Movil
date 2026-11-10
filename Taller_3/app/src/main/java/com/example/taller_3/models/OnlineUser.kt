package com.example.taller_3.models

/** Usuario en línea junto con su uid, que es la llave con la que se guarda en la base de datos. */
data class OnlineUser(
    val uid: String,
    val user: User,
)
