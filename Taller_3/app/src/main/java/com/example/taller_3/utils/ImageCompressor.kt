package com.example.taller_3.utils

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Size
import java.io.File

/**
 * Reduce el tamaño de la foto de perfil antes de subirla a Firebase Storage.
 * La cámara guarda fotos de varios MB y para un avatar o un marcador del mapa basta con ~1024 px,
 * así el registro y el cambio de foto suben mucho menos datos.
 */
object ImageCompressor {
    private const val MAX_SIDE = 1024
    private const val JPEG_QUALITY = 85

    /**
     * Retorna el Uri de una copia reducida de la imagen, o el mismo Uri si no se pudo reducir.
     * Debe llamarse desde un hilo secundario.
     */
    fun compress(context: Context, uri: Uri): Uri {
        // loadThumbnail respeta la orientación de la foto, pero solo existe desde Android 10
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return uri
        return try {
            val bitmap = context.contentResolver.loadThumbnail(uri, Size(MAX_SIDE, MAX_SIDE), null)
            val file = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, it) }
            Uri.fromFile(file)
        } catch (e: Exception) {
            uri
        }
    }
}
