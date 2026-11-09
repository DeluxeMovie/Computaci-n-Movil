package com.example.taller_3.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taller_3.ui.components.UserPortrait
import com.example.taller_3.utils.DateUtil
import com.example.taller_3.viewmodels.AuthViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        UserPortrait(
            imageUrl = currentUser?.photoUrl?.toString(),
            name = currentUser?.displayName?.takeIf { it.isNotBlank() }
                ?: currentUser?.email
                ?: "Guest",
            lastLogin = currentUser?.metadata?.lastSignInTimestamp?.let(DateUtil::format) ?: "-",
            modifier = Modifier.padding(16.dp)
        )
    }
}
