package com.example.taller_1.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.taller_1.data.model.Product

/** Etiqueta común para los registros (logcat) de los intents del producto. */
private const val TAG = "ProductIntent"

/**
 * Abre la aplicación de teléfono con el marcador listo para llamar.
 *
 * Lanza un intent [Intent.ACTION_DIAL] cuyo dato es `tel:` seguido del id del
 * producto, de modo que la app de llamadas muestre ese id como número.
 *
 * @param context contexto usado para iniciar la actividad externa.
 * @param product producto cuyo id se coloca en el marcador.
 */
fun dialProduct(context: Context, product: Product) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:${product.id}")
    }
    launch(context, intent)
}

/**
 * Abre la aplicación de mensajería (SMS) con el texto del mensaje ya escrito.
 *
 * Lanza un intent [Intent.ACTION_SEND] con la forma `smsto:` y coloca como
 * cuerpo del mensaje el texto "title: description" del producto. No se adjunta
 * ningún archivo al intent.
 *
 * @param context contexto usado para iniciar la actividad externa.
 * @param product producto cuyo título y descripción forman el mensaje.
 */
fun sendProductSms(context: Context, product: Product) {
    val message = "${product.title}: ${product.description}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        data = Uri.parse("smsto:")
        type = "text/plain"
        putExtra("sms_body", message)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    launch(context, intent)
}

/**
 * Inicia la actividad externa asociada al [intent] y avisa al usuario con un
 * Toast si el teléfono no tiene ninguna aplicación capaz de atenderlo.
 */
private fun launch(context: Context, intent: Intent) {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e(TAG, "No hay una aplicación que pueda atender el intent", e)
        Toast.makeText(context, "No hay una app disponible para esta acción", Toast.LENGTH_SHORT)
            .show()
    }
}
