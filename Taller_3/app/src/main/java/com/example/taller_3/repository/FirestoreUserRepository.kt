package com.example.taller_3.repository

import com.example.taller_3.data.RoutePoint
import com.example.taller_3.data.UserInfo
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.SetOptions

class FirestoreUserRepository(
    db: FirebaseFirestore = FirebaseFirestore.getInstance(DATABASE_ID)
) : UserRepository {

    private val users = db.collection(USERS)

    override fun save(uid: String, info: UserInfo, onResult: (Result<Unit>) -> Unit) {
        users.document(uid).set(info.toMap(), SetOptions.merge()).addOnCompleteListener { onResult(it.toUnitResult()) }
    }

    override fun get(uid: String, onResult: (Result<UserInfo?>) -> Unit) {
        users.document(uid).get().addOnCompleteListener { task ->
            onResult(
                if (task.isSuccessful) Result.success(task.result?.toUserInfo())
                else Result.failure(task.exception ?: UnknownError())
            )
        }
    }

    override fun observe(uid: String, onChange: (Result<UserInfo?>) -> Unit): Subscription {
        val registration = users.document(uid).addSnapshotListener { snap, e ->
            onChange(if (e != null) Result.failure(e) else Result.success(snap?.toUserInfo()))
        }
        return Subscription { registration.remove() }
    }

    // Se usa merge para que también funcione con cuentas que aún no tienen documento en Firestore
    override fun update(uid: String, fields: Map<String, Any?>, onResult: (Result<Unit>) -> Unit) {
        users.document(uid).set(fields, SetOptions.merge()).addOnCompleteListener { onResult(it.toUnitResult()) }
    }

    override fun delete(uid: String, onResult: (Result<Unit>) -> Unit) {
        users.document(uid).delete().addOnCompleteListener { onResult(it.toUnitResult()) }
    }

    override fun observeOnlineUsers(onChange: (Result<Map<String, UserInfo>>) -> Unit): Subscription {
        // Cuando un usuario se desconecta deja de cumplir la consulta y desaparece del resultado
        val registration = users.whereEqualTo("online", true).addSnapshotListener { snap, e ->
            if (e != null) {
                onChange(Result.failure(e))
                return@addSnapshotListener
            }
            val onlineUsers = snap?.documents.orEmpty()
                .mapNotNull { document -> document.toUserInfo()?.let { document.id to it } }
                .toMap()
            onChange(Result.success(onlineUsers))
        }
        return Subscription { registration.remove() }
    }

    override fun updateLocation(
        uid: String, latitude: Double, longitude: Double, onResult: (Result<Unit>) -> Unit,
    ) {
        val fields = mapOf(
            "online" to true,
            "latitude" to latitude,
            "longitude" to longitude,
            "route" to FieldValue.arrayUnion(GeoPoint(latitude, longitude)),
            "lastSeen" to System.currentTimeMillis(),
        )
        users.document(uid).set(fields, SetOptions.merge()).addOnCompleteListener { onResult(it.toUnitResult()) }
    }

    // La ubicación, el recorrido y la última vez en línea no se envían al registrarse
    private fun UserInfo.toMap() = mapOf(
        "displayName" to displayName,
        "photo" to photo,
        "phone" to phone,
        "online" to online,
    )

    private fun DocumentSnapshot.toUserInfo(): UserInfo? {
        if (!exists()) return null
        return UserInfo(
            displayName = getString("displayName").orEmpty(),
            photo = getString("photo"),
            phone = getLong("phone") ?: 0L,
            online = getBoolean("online") ?: false,
            latitude = getDouble("latitude"),
            longitude = getDouble("longitude"),
            route = (get("route") as? List<*>).orEmpty()
                .filterIsInstance<GeoPoint>()
                .map { RoutePoint(it.latitude, it.longitude) },
            lastSeen = getLong("lastSeen") ?: 0L,
        )
    }

    private companion object {
        const val USERS = "users"
        // Nombre de la base de datos creada en la consola de Firebase (no es la base "(default)")
        const val DATABASE_ID = "taller-3"
    }
}

internal fun Task<*>.toUnitResult(): Result<Unit> =
    if (isSuccessful) Result.success(Unit) else Result.failure(exception ?: UnknownError())
