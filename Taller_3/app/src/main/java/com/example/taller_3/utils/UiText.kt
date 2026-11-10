package com.example.taller_3.utils

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.taller_3.R

/**
 * Texto que muestra la interfaz. Los ViewModels no tienen contexto para leer strings.xml,
 * por eso guardan el id del recurso (o el texto que ya viene de Firebase) y la pantalla lo convierte.
 */
sealed interface UiText {
    data class Dynamic(val value: String) : UiText
    data class Resource(@StringRes val id: Int) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> value
    is UiText.Resource -> stringResource(id)
}

/** Usa el mensaje de Firebase si existe, si no un mensaje de error genérico. */
fun Throwable?.toUiText(): UiText =
    this?.localizedMessage?.let { UiText.Dynamic(it) } ?: UiText.Resource(R.string.error_unknown)
