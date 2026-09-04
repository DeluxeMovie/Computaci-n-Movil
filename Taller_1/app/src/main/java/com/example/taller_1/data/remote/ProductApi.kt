package com.example.taller_1.data.remote

import android.util.Log
import com.example.taller_1.data.model.Product
import com.example.taller_1.data.model.ProductsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Cliente de red que consulta el API de productos usando Ktor.
 *
 * El cliente se configura una sola vez con:
 *  - motor OkHttp,
 *  - registro de peticiones (Logging),
 *  - negociación de contenido JSON que ignora las claves desconocidas para no
 *    fallar con los campos del JSON que la aplicación no utiliza.
 */
class ProductApi {

    private val client = HttpClient(OkHttp) {
        defaultRequest {
            url("https://dummyjson.com/")
        }

        install(Logging) {
            logger = Logger.SIMPLE
        }

        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }

    /**
     * Solicita al API la lista de productos.
     *
     * Realiza una petición GET a `products?limit=194`, deserializa la respuesta
     * en un [ProductsResponse] y retorna la lista contenida en él. Si ocurre
     * cualquier error de red o de parseo, registra el problema y retorna una
     * lista vacía para que la interfaz pueda mostrar un estado de error.
     *
     * @return la lista de [Product] recibida, o una lista vacía si falla.
     */
    suspend fun getProducts(): List<Product> = try {
        val response: ProductsResponse = client
            .get("products?limit=194")
            .body()
        response.products
    } catch (e: Exception) {
        Log.e("ProductApi", "Error al consultar el API de productos", e)
        emptyList()
    }
}
