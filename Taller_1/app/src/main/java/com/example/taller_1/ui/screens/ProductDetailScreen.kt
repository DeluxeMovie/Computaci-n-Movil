package com.example.taller_1.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.taller_1.R
import com.example.taller_1.data.model.Product
import com.example.taller_1.ui.components.DetailSection
import com.example.taller_1.util.dialProduct
import com.example.taller_1.util.sendProductSms
import java.util.Locale

/**
 * Segunda pantalla: muestra la información detallada del producto seleccionado.
 *
 * Recibe el objeto [Product] completo desde la ruta de navegación, por lo que no
 * realiza ninguna consulta al API. Presenta siete campos del JSON: título,
 * descripción, categoría, etiquetas, dimensiones, reseñas e imagenes.
 *
 * Interacciones con intents:
 *  - al tocar el título se llama a [dialProduct], que abre la app de telefono
 *    (Intent.ACTION_DIAL) con el id del producto;
 *  - al tocar la descripción se llama a [sendProductSms], que abre la app de SMS
 *    (Intent.ACTION_SEND, forma "smsto:") con el texto "title: description".
 *
 * @param product producto cuya información se muestra.
 * @param onBack acción para regresar a la pantalla anterior.
 * @param modifier modificador aplicado al contenedor de la pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    product: Product,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Contexto necesario para lanzar los intents hacia otras apps del teléfono.
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Campo 1: título. Al tocarlo se ejecuta el intent de llamada.
            DetailSection(
                label = stringResource(R.string.detail_field_title),
                onClick = { dialProduct(context, product) },
            ) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = stringResource(R.string.detail_hint_call),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            // Campo 2: descripción. Al tocarla se ejecuta el intent de SMS.
            DetailSection(
                label = stringResource(R.string.detail_field_description),
                onClick = { sendProductSms(context, product) },
            ) {
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(R.string.detail_hint_sms),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            // Campo 3: categoría.
            DetailSection(label = stringResource(R.string.detail_field_category)) {
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Campo 4: etiquetas (tags).
            DetailSection(label = stringResource(R.string.detail_field_tags)) {
                Text(
                    text = product.tags.joinToString(separator = ", "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Campo 5: dimensiones (ancho, alto y profundidad).
            DetailSection(label = stringResource(R.string.detail_field_dimensions)) {
                Text(
                    text = stringResource(
                        R.string.detail_dimensions_value,
                        String.format(Locale.US, "%.2f", product.dimensions.width),
                        String.format(Locale.US, "%.2f", product.dimensions.height),
                        String.format(Locale.US, "%.2f", product.dimensions.depth),
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Campo 6: reseñas. Se lista cada reseña recibida.
            DetailSection(label = stringResource(R.string.detail_field_reviews)) {
                if (product.reviews.isEmpty()) {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    product.reviews.forEach { review ->
                        Text(
                            text = stringResource(
                                R.string.detail_review_item,
                                review.rating.toString(),
                                review.comment,
                                review.reviewerName,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Campo 7: imágenes. Se muestran en una fila desplazable con Coil.
            DetailSection(label = stringResource(R.string.detail_field_images)) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(product.images) { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = stringResource(
                                R.string.content_desc_thumbnail,
                                product.title,
                            ),
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.width(160.dp),
                        )
                    }
                }
            }
        }
    }
}
