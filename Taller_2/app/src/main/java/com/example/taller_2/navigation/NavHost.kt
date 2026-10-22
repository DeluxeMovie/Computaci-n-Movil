package com.example.taller_2.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taller_2.ui.screens.HomeScreen

/**
 * Host de navegación principal. Guarda la pila de navegación y muestra
 * la entrada actual con [NavDisplay].
 */
@Composable
fun NavHost(modifier: Modifier = Modifier) {
    val backStack = rememberNavBackStack(Routes.Home)

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        NavDisplay(
            modifier = Modifier.padding(paddingValues),
            backStack = backStack,
            onBack = {
                backStack.removeLastOrNull()
            },
            entryProvider = { key ->
                when (key) {
                    is Routes.Home -> NavEntry(key) {
                        HomeScreen()
                    }

                    else -> {
                        error("Unknown route: $key")
                    }
                }
            }
        )
    }
}
