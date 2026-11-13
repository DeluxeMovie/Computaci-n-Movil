package com.example.taller_3.repository

import com.example.taller_3.data.UserInfo

fun interface Subscription {
    fun cancel()
}

interface UserRepository {
    fun save(uid: String, info: UserInfo, onResult: (Result<Unit>) -> Unit)
    fun get(uid: String, onResult: (Result<UserInfo?>) -> Unit)
    fun observe(uid: String, onChange: (Result<UserInfo?>) -> Unit): Subscription
    fun update(uid: String, fields: Map<String, Any?>, onResult: (Result<Unit>) -> Unit)
    fun delete(uid: String, onResult: (Result<Unit>) -> Unit)

    // Todos los usuarios que tienen la localización encendida, por uid. Se actualiza en tiempo real.
    fun observeOnlineUsers(onChange: (Result<Map<String, UserInfo>>) -> Unit): Subscription

    // Guarda la posición actual y la agrega al recorrido del usuario.
    fun updateLocation(uid: String, latitude: Double, longitude: Double, onResult: (Result<Unit>) -> Unit)
}
