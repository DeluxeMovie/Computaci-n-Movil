package com.example.taller_1.data.model

import kotlinx.serialization.Serializable

/**
 * Representa un producto devuelto por el API https://dummyjson.com/products.
 *
 * Solo se declaran los campos que la aplicación necesita mostrar. La clase está
 * anotada con [Serializable] por dos motivos:
 *  1. Ktor la usa para convertir el JSON de la respuesta en objetos Kotlin.
 *  2. Navigation 3 la usa para enviar el objeto completo como parámetro de la
 *     ruta hacia la pantalla de detalle.
 */
@Serializable
data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val thumbnail: String,
    val tags: List<String> = emptyList(),
    val dimensions: Dimensions = Dimensions(),
    val reviews: List<Review> = emptyList(),
    val images: List<String> = emptyList(),
)

/**
 * Dimensiones físicas del producto (ancho, alto y profundidad) tal como vienen
 * en el campo "dimensions" del JSON.
 */
@Serializable
data class Dimensions(
    val width: Double = 0.0,
    val height: Double = 0.0,
    val depth: Double = 0.0,
)

/**
 * Reseña individual de un producto, correspondiente a cada elemento del arreglo
 * "reviews" del JSON.
 */
@Serializable
data class Review(
    val rating: Int = 0,
    val comment: String = "",
    val date: String = "",
    val reviewerName: String = "",
    val reviewerEmail: String = "",
)

/**
 * Envoltura de la respuesta del API. El endpoint devuelve un objeto con la lista
 * de productos dentro de la propiedad "products"; esta clase permite que Ktor la
 * deserialice directamente.
 */
@Serializable
data class ProductsResponse(
    val products: List<Product> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0,
)
