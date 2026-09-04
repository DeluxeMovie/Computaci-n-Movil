package com.example.taller_1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller_1.R
import com.example.taller_1.data.model.Product
import com.example.taller_1.ui.theme.AppTheme
import java.util.Locale

/**
 * Componente de interfaz reutilizable de un producto de la primera pantalla.
 *
 * Muestra la imagen (thumbnail), el nombre (title), la categoría, el precio y la
 * calificación del producto. Está construido con un [Card] y modificadores de
 * Compose.
 *
 * @param product producto que se va a dibujar.
 * @param onClick acción que se ejecuta al tocar la tarjeta; navega al detalle.
 * @param modifier modificador aplicado a la tarjeta.
 */
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            // Imagen del producto obtenida de la URL "thumbnail" mediante Coil.
            AsyncImage(
                model = product.thumbnail,
                contentDescription = stringResource(
                    R.string.content_desc_thumbnail,
                    product.title,
                ),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
            )

            // Nombre del producto.
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Categoría del producto.
            Text(
                text = stringResource(R.string.label_category, product.category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )

            // Fila inferior: precio a la izquierda y calificación a la derecha.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        R.string.label_price,
                        String.format(Locale.US, "%.2f", product.price),
                    ),
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = String.format(Locale.US, "%.1f", product.rating),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ProductCardPreview() {
    AppTheme {
        ProductCard(
            product = Product(
                id = 1,
                title = "Essence Mascara Lash Princess",
                description = "Máscara de pestañas conocida por su efecto voluminizador.",
                category = "beauty",
                price = 9.99,
                rating = 2.56,
                thumbnail = "",
            ),
            onClick = {},
        )
    }
}
