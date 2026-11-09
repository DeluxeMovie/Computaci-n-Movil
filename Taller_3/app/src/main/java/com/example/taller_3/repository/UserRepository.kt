package com.example.taller_3.repository

import com.example.taller_3.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val users = database.getReference("users")

    fun saveUser(uid: String, user: User): Task<Void> = users.child(uid).setValue(user)

    fun observeUser(uid: String, listener: ValueEventListener) {
        users.child(uid).addValueEventListener(listener)
    }

    fun stopObservingUser(uid: String, listener: ValueEventListener) {
        users.child(uid).removeEventListener(listener)
    }

    fun updateProfile(uid: String, name: String, phone: String): Task<Void> =
        users.child(uid).updateChildren(mapOf("name" to name, "phone" to phone))
}
