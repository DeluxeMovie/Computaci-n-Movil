package com.example.taller_3.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Size
import coil3.toBitmap

/**
 * Utilidades para crear la imagen que se muestra como marcador en el mapa.
 */
object MarkerBitmapUtils {
    private const val MARKER_SIZE = 120
    private const val BORDER_SIZE = 8f

    /**
     * Crea un marcador circular de color con borde blanco y la inicial del nombre en el centro.
     * Se usa mientras el usuario no tiene foto o la foto aún se está descargando.
     */
    fun createUserMarker(name: String, color: Int): Bitmap {
        val marker = Bitmap.createBitmap(MARKER_SIZE, MARKER_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(marker)
        val center = MARKER_SIZE / 2f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = Color.WHITE }
        canvas.drawCircle(center, center, center, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
        canvas.drawCircle(center, center, center - BORDER_SIZE, fillPaint)

        val initial = name.trim().firstOrNull()?.uppercase() ?: "?"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            textSize = MARKER_SIZE * 0.5f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        // Centra el texto verticalmente usando la altura real de la letra
        val bounds = Rect()
        textPaint.getTextBounds(initial, 0, initial.length, bounds)
        canvas.drawText(initial, center, center - bounds.exactCenterY(), textPaint)
        return marker
    }

    /**
     * Crea un marcador circular con la foto de perfil. El borde es del color del usuario
     * para distinguir al usuario actual de los demás.
     */
    fun createPhotoMarker(photo: Bitmap, borderColor: Int): Bitmap {
        val marker = Bitmap.createBitmap(MARKER_SIZE, MARKER_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(marker)
        val center = MARKER_SIZE / 2f

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = borderColor }
        canvas.drawCircle(center, center, center, borderPaint)

        // Recorta la foto al centro para que quede cuadrada y la dibuja dentro del borde
        val innerSize = MARKER_SIZE - 2 * BORDER_SIZE.toInt()
        val side = minOf(photo.width, photo.height)
        val square = Bitmap.createBitmap(
            photo, (photo.width - side) / 2, (photo.height - side) / 2, side, side
        )
        val scaled = Bitmap.createScaledBitmap(square, innerSize, innerSize, true)
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.translate(BORDER_SIZE, BORDER_SIZE)
        canvas.drawCircle(innerSize / 2f, innerSize / 2f, innerSize / 2f, photoPaint)
        return marker
    }

    /**
     * Descarga la foto de perfil con Coil (usa su caché). Retorna null si no se pudo descargar.
     * Debe llamarse desde un hilo secundario.
     */
    suspend fun loadPhoto(context: Context, url: String): Bitmap? {
        val request = ImageRequest.Builder(context)
            .data(url)
            .size(Size(MARKER_SIZE * 2, MARKER_SIZE * 2))
            // Los bitmaps de hardware no se pueden recortar ni dibujar en un canvas
            .allowHardware(false)
            .build()
        val result = SingletonImageLoader.get(context).execute(request)
        return (result as? SuccessResult)?.image?.toBitmap()
    }
}
