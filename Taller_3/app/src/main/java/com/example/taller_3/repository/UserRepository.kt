package com.example.taller_3.repository

import com.example.taller_3.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase

class UserRepository(
    database: FirebaseDatabase = FirebaseDatabase.getInstance(),
) {
    private val users = database.getReference("users")

    fun saveUser(uid: String, user: User): Task<Void> = users.child(uid).setValue(user)
}
