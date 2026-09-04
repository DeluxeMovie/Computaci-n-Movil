package com.example.taller_1.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Esquema de color de respaldo para el modo claro (dispositivos sin Material You).
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

// Esquema de color de respaldo para el modo oscuro (dispositivos sin Material You).
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
 * Selección del esquema de color:
 *  1. Si [dynamicColor] está activo y el dispositivo es Android 12 (API 31) o
 *     superior, se usa Dynamic Color / Material You: los colores se generan a
 *     partir del fondo de pantalla del usuario con [dynamicLightColorScheme] o
 *     [dynamicDarkColorScheme].
 *  2. En cualquier otro caso se usa el esquema de respaldo definido en este
 *     archivo ([LightColors] o [DarkColors]).
 *
 * En ambos casos se respeta el modo claro u oscuro del sistema mediante
 * [isSystemInDarkTheme].
 *
 * @param darkTheme indica si se debe usar el esquema oscuro; por defecto toma el
 *   valor del sistema.
 * @param dynamicColor indica si se permite Dynamic Color; activo por defecto.
 * @param content contenido Composable al que se le aplica el tema.
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
