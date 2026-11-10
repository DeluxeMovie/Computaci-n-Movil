package com.example.taller_3.repository

import android.net.Uri
import com.example.taller_3.models.OnlineUser
import com.example.taller_3.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(DATABASE_ID),
    storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private val users = firestore.collection("users")
    private val photos = storage.reference.child("profile_photos")

    fun saveUser(uid: String, user: User): Task<Void> = users.document(uid).set(user)

    /**
     * Crea el documento del usuario si no existe, por ejemplo en cuentas que se registraron antes
     * de usar Firestore. Se hace en una transacción para no pisar los datos de un registro en curso.
     */
    fun ensureUserExists(uid: String, email: String): Task<Void?> {
        val document = users.document(uid)
        return firestore.runTransaction { transaction ->
            if (!transaction.get(document).exists()) {
                transaction.set(document, User(email = email))
            }
            null
        }
    }

    /** Escucha los cambios del documento del usuario. Hay que llamar remove() al terminar. */
    fun observeUser(
        uid: String,
        onChange: (User?) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration = users.document(uid).addSnapshotListener { snapshot, error ->
        if (error != null) onError(error)
        else onChange(snapshot?.toObject(User::class.java))
    }

    /**
     * Escucha a todos los usuarios en línea. Cuando uno se desconecta deja de cumplir la
     * consulta y desaparece de la lista.
     */
    fun observeOnlineUsers(
        onChange: (List<OnlineUser>) -> Unit,
        onError: (Exception) -> Unit,
    ): ListenerRegistration = users.whereEqualTo("online", true)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError(error)
                return@addSnapshotListener
            }
            onChange(
                snapshot?.documents.orEmpty().mapNotNull { document ->
                    document.toObject(User::class.java)?.let { OnlineUser(document.id, it) }
                }
            )
        }

    fun updateProfile(uid: String, name: String, phone: String): Task<Void> =
        users.document(uid).update(mapOf("name" to name, "phone" to phone))

    /** Sube la foto a Storage (profile_photos/{uid}.jpg) y devuelve su URL de descarga. */
    fun uploadPhoto(uid: String, uri: Uri): Task<Uri> {
        val photoRef = photos.child("$uid.jpg")
        return photoRef.putFile(uri).continueWithTask { upload ->
            if (!upload.isSuccessful) throw upload.exception!!
            photoRef.downloadUrl
        }
    }

    fun updatePhotoUrl(uid: String, url: String): Task<Void> =
        users.document(uid).update("photoUrl", url)

    /** Marca al usuario en línea y empieza un recorrido nuevo. */
    fun startSharing(uid: String): Task<Void> =
        users.document(uid).update(
            mapOf(
                "online" to true,
                "route" to emptyList<GeoPoint>(),
                "lastSeen" to System.currentTimeMillis(),
            )
        )

    /** Guarda la posición actual y la agrega al recorrido. */
    fun updateLocation(uid: String, latitude: Double, longitude: Double): Task<Void> =
        users.document(uid).update(
            mapOf(
                "online" to true,
                "latitude" to latitude,
                "longitude" to longitude,
                "route" to FieldValue.arrayUnion(GeoPoint(latitude, longitude)),
                "lastSeen" to System.currentTimeMillis(),
            )
        )

    /** Avisa que el usuario sigue en línea aunque no se haya movido. */
    fun keepAlive(uid: String): Task<Void> =
        users.document(uid).update("lastSeen", System.currentTimeMillis())

    /** Marca al usuario fuera de línea y borra su recorrido y su posición. */
    fun setOffline(uid: String): Task<Void> =
        users.document(uid).update(
            mapOf(
                "online" to false,
                "route" to emptyList<GeoPoint>(),
                "latitude" to FieldValue.delete(),
                "longitude" to FieldValue.delete(),
            )
        )

    private companion object {
        // Nombre de la base de datos creada en la consola de Firebase (no es la base "(default)")
        const val DATABASE_ID = "taller-3"
    }
}
