package com.example.taller_1.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taller_1.R
import com.example.taller_1.data.model.Product
import com.example.taller_1.ui.components.ProductCard

/**
 * Primera pantalla de la aplicación: muestra la lista de productos.
 *
 * Los productos se dibujan dentro de un [LazyVerticalGrid] de dos columnas,
 * usando el componente reutilizable [ProductCard]. La cuadrícula tiene un
 * encabezado fijo (StickyHeader) que indica el número total de productos.
 *
 * Esta pantalla no consulta el API: recibe la lista ya cargada por el host de
 * navegación.
 *
 * @param products lista de productos a mostrar.
 * @param isLoading indica si todavía se está esperando la respuesta del API.
 * @param isError indica si la carga terminó sin productos es decir si hubo un error de conexión.
 * @param onProductClick se invoca con el producto tocado para navegar al detalle.
 * @param modifier modificador aplicado al contenedor de la pantalla.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    products: List<Product>,
    isLoading: Boolean,
    isError: Boolean,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        // Estado de carga: indicador de progreso centrado.
        isLoading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
                Text(text = stringResource(R.string.loading_products))
            }
        }

        // Estado de error: mensaje centrado.
        isError -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.error_products),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }

        // Estado con datos: cuadrícula de productos.
        else -> {
            val gridState = rememberLazyGridState()
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Encabezado fijo con el número total de productos mostrados.
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.background)
                            .padding(vertical = 8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.products_header, products.size),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                // Un ProductCard por cada producto de la lista.
                items(
                    items = products,
                    key = { product -> product.id },
                ) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) },
                    )
                }
            }
        }
    }
}
