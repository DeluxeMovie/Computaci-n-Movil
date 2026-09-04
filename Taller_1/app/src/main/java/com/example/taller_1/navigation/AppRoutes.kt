package com.example.taller_1.navigation

import androidx.navigation3.runtime.NavKey
import com.example.taller_1.data.model.Product
import kotlinx.serialization.Serializable

/**
 * Rutas que identifican cada pantalla a la que el usuario puede navegar.
 * Cada ruta se agrega o se quita de la pila de navegación administrada por
 * `NavDisplay`.
 *
 * Las rutas implementan [NavKey] y están anotadas con [Serializable] para que la
 * pila de navegación se pueda conservar ante cambios de configuración.
 */

/** Ruta de la primera pantalla: la lista de productos. */
@Serializable
data object ProductListRoute : NavKey

/**
 * Ruta de la pantalla de detalle. Transporta el objeto [Product] completo como
 * parámetro.
 */
@Serializable
data class ProductDetailRoute(val product: Product) : NavKey
