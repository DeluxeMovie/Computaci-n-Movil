package com.example.taller_2.data

import android.net.Uri

/**
 * Foto tomada durante el recorrido.
 *
 * @param name nombre del archivo de la foto, también se usa como título del marcador en el mapa.
 * @param uri ubicación de la foto en la galería del dispositivo.
 * @param latitude latitud donde se tomó la foto, es nula si aún no se conocía la ubicación.
 * @param longitude longitud donde se tomó la foto, es nula si aún no se conocía la ubicación.
 */
data class RoutePhoto(
    val name: String,
    val uri: Uri,
    val latitude: Double?,
    val longitude: Double?
)
