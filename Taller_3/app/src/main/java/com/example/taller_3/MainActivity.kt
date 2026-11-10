package com.example.taller_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.taller_3.navigation.NavHost
import com.example.taller_3.ui.theme.Taller_3Theme
import com.mapbox.common.MapboxOptions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // El token de Mapbox se guarda en secrets.properties para que no se suba a GitHub
        MapboxOptions.accessToken = BuildConfig.MAPBOX_ACCESS_TOKEN
        setContent {
            Taller_3Theme {
                NavHost()
            }
        }

        // Use Firebase emulator for testing
//        val host  = "10.195.69.141"
//        try {
//            Firebase.auth.useEmulator(host, 9099) // Firebase Auth Emulator
//            Log.d("MainActivity", "Using Firebase Auth Emulator at $host:9000")
//        } catch (e: Exception) {
//            Log.e("MainActivity", "Error setting up Firebase Auth Emulator", e)
//        }
    }
}
