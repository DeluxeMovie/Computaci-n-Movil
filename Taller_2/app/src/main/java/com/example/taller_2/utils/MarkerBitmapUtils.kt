package com.example.taller_2.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.media.ThumbnailUtils
import android.net.Uri
import android.util.Size

/**
 * Utilidades para crear la imagen que se muestra como marcador en el mapa.
 */
object MarkerBitmapUtils {
    private const val MARKER_SIZE = 160
    private const val BORDER_SIZE = 8
    private const val CORNER_RADIUS = 24f

    /**
     * Crea una miniatura cuadrada con esquinas redondeadas y borde blanco a partir de la foto.
     * Si la foto no se puede leer, retorna un marcador gris para que el mapa siempre muestre algo.
     */
    fun createPhotoMarker(context: Context, photoUri: Uri): Bitmap {
        val innerSize = MARKER_SIZE - 2 * BORDER_SIZE
        val thumbnail = try {
            val original = context.contentResolver.loadThumbnail(
                photoUri,
                Size(MARKER_SIZE, MARKER_SIZE),
                null
            )
            // Recorta la miniatura al centro para que quede cuadrada
            ThumbnailUtils.extractThumbnail(original, innerSize, innerSize)
        } catch (e: Exception) {
            Bitmap.createBitmap(innerSize, innerSize, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.GRAY)
            }
        }

        val marker = Bitmap.createBitmap(MARKER_SIZE, MARKER_SIZE, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(marker)

        // Dibuja el fondo blanco que sirve como borde
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        canvas.drawRoundRect(
            RectF(0f, 0f, MARKER_SIZE.toFloat(), MARKER_SIZE.toFloat()),
            CORNER_RADIUS,
            CORNER_RADIUS,
            borderPaint
        )

        // Dibuja la foto dentro del borde
        val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(thumbnail, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.translate(BORDER_SIZE.toFloat(), BORDER_SIZE.toFloat())
        canvas.drawRoundRect(
            RectF(0f, 0f, innerSize.toFloat(), innerSize.toFloat()),
            CORNER_RADIUS - BORDER_SIZE / 2,
            CORNER_RADIUS - BORDER_SIZE / 2,
            photoPaint
        )
        return marker
    }
}
