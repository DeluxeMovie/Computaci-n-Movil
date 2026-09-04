package com.example.taller_1.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Componente de interfaz reutilizable que muestra un campo del producto dentro
 * de la pantalla de detalle: un label en la parte superior y, debajo,
 * el contenido entregado como slot.
 *
 * Si se pasa [onClick], toda la tarjeta se vuelve clickeable, esto se usa para
 * los campos que disparan un intent.
 *
 * @param label Nombre del campo (por ejemplo "Título", "Descripción").
 * @param modifier modificador aplicado a la tarjeta.
 * @param onClick acción opcional que se ejecuta al tocar la tarjeta.
 * @param content contenido del campo, normalmente uno o varios [Text].
 */
@Composable
fun DetailSection(
    label: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable { onClick() }
    } else {
        modifier.fillMaxWidth()
    }

    Card(modifier = cardModifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            content()
        }
    }
}
