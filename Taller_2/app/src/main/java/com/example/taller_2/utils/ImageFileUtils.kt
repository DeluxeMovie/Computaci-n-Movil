package com.example.taller_2.utils

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore

/**
 * Utilidades para crear las fotos del recorrido en la galería del dispositivo.
 */
object ImageFileUtils {
    /**
     * Crea el nombre de una foto nueva, por ejemplo ROUTE_IMG_1774883431126.jpg.
     */
    fun createImageName(): String {
        return "ROUTE_IMG_" + System.currentTimeMillis() + ".jpg"
    }

    /**
     * Crea los datos que MediaStore necesita para guardar la foto en la carpeta
     * Pictures/Taller2 de la galería.
     */
    fun createContentValues(imageName: String): ContentValues {
        return ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, imageName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Taller2"
            )
        }
    }
}
