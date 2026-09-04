package com.example.taller_1.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.taller_1.data.model.Product
import com.example.taller_1.data.remote.ProductApi
import com.example.taller_1.ui.screens.ProductDetailScreen
import com.example.taller_1.ui.screens.ProductListScreen

/**
 * Host de navegación de la aplicación:
 * administra la pila de navegación y decide qué pantalla se dibuja.
 *
 * Aquí también se realiza la consulta al API. La lista obtenida se
 * guarda en estado y se pasa a la pantalla de lista, la pantalla de detalle
 * recibe el objeto [Product] por la ruta, por lo que nunca vuelve a consultar
 * el API.
 *
 * @param modifier modificador aplicado al contenedor de navegación.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    // Pila de navegación. Empieza en la pantalla de lista de productos.
    val backStack = rememberNavBackStack(ProductListRoute)

    // Estado compartido de la lista de productos.
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }

    // Se ejecuta una sola vez (clave fija Unit) durante toda la vida del host.
    LaunchedEffect(Unit) {
        val api = ProductApi()
        val fetched = api.getProducts()
        isError = fetched.isEmpty()
        products = fetched
        isLoading = false
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = modifier,
        entryProvider = entryProvider {
            // Primera pantalla: lista de productos.
            entry<ProductListRoute> {
                ProductListScreen(
                    products = products,
                    isLoading = isLoading,
                    isError = isError,
                    onProductClick = { product ->
                        backStack.add(ProductDetailRoute(product))
                    }
                )
            }
            // Segunda pantalla: detalle del producto seleccionado.
            entry<ProductDetailRoute> { route ->
                ProductDetailScreen(
                    product = route.product,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
