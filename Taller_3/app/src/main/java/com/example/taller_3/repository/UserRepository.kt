package com.example.taller_3.repository

import android.net.Uri
import com.example.taller_3.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class UserRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private val users = database.getReference("users")
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
}
