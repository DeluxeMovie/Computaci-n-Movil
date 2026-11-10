package com.example.taller_3.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect

/**
 * Utilidades para crear la imagen que se muestra como marcador en el mapa.
 */
object MarkerBitmapUtils {
    private const val MARKER_SIZE = 120
    private const val BORDER_SIZE = 8f

    /**
     * Crea un marcador circular de color con borde blanco y la inicial del nombre en el centro.
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
}
