package com.example.taller_2.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Rutas (llaves) que identifican el contenido al que el usuario puede navegar.
 * Cada ruta se agrega o se quita de la pila de navegación que maneja [NavDisplay].
 *
 * Las rutas son `@Serializable` e implementan [NavKey] para que la pila se pueda
 * guardar con `rememberNavBackStack` y sobreviva a cambios de configuración.
 */
@Serializable
sealed class Routes : NavKey {
    @Serializable data object Home : Routes()
}
