package com.example.taller_3.repository

import android.net.Uri
import com.example.taller_3.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class UserRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private val users = database.getReference("users")
    // Máximo 100 usuarios en línea a la vez
    private val onlineUsers: Query = users.orderByChild("online").equalTo(true).limitToFirst(MAX_ONLINE_USERS)
    private val photos = storage.reference.child("profile_photos")

    fun saveUser(uid: String, user: User): Task<Void> = users.child(uid).setValue(user)

    fun observeUser(uid: String, listener: ValueEventListener) {
        users.child(uid).addValueEventListener(listener)
    }

    fun stopObservingUser(uid: String, listener: ValueEventListener) {
        users.child(uid).removeEventListener(listener)
    }

    fun updateProfile(uid: String, name: String, phone: String): Task<Void> =
        users.child(uid).updateChildren(mapOf("name" to name, "phone" to phone))

    /** Sube la foto a Storage (profile_photos/{uid}.jpg) y devuelve su URL de descarga. */
    fun uploadPhoto(uid: String, uri: Uri): Task<Uri> {
        val photoRef = photos.child("$uid.jpg")
        return photoRef.putFile(uri).continueWithTask { upload ->
            if (!upload.isSuccessful) throw upload.exception!!
            photoRef.downloadUrl
        }
    }

    fun updatePhotoUrl(uid: String, url: String): Task<Void> =
        users.child(uid).child("photoUrl").setValue(url)

    fun observeOnlineUsers(listener: ValueEventListener) {
        onlineUsers.addValueEventListener(listener)
    }

    fun stopObservingOnlineUsers(listener: ValueEventListener) {
        onlineUsers.removeEventListener(listener)
    }

    /** Guarda la ubicación actual y marca al usuario en línea. */
    fun updateLocation(uid: String, latitude: Double, longitude: Double): Task<Void> =
        users.child(uid).updateChildren(
            mapOf("online" to true, "latitude" to latitude, "longitude" to longitude)
        )

    fun setOffline(uid: String): Task<Void> = users.child(uid).child("online").setValue(false)

    /** Si la app se cierra o se pierde la conexión, el servidor marca al usuario como fuera de línea. */
    fun setOfflineOnDisconnect(uid: String) {
        users.child(uid).child("online").onDisconnect().setValue(false)
    }

    private companion object {
        const val MAX_ONLINE_USERS = 100
    }
}
