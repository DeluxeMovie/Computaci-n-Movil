package com.example.taller_1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Esquema de color usado cuando el sistema está en modo claro.
private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    secondary = SecondaryLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    error = ErrorLight,
)

// Esquema de color usado cuando el sistema está en modo oscuro.
private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = SecondaryDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
)

/**
 * Aplica el tema Material 3 de la aplicación al contenido recibido.
 *
 * Consulta si el sistema está en modo oscuro con [isSystemInDarkTheme] y, según
 * ese valor, entrega el esquema de color claro u oscuro.
 *
 * @param darkTheme indica si se debe usar el esquema oscuro; por defecto toma el
 *   valor del sistema.
 * @param content contenido Composable al que se le aplica el tema.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
