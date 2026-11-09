package com.example.taller_3.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DateUtil {
    companion object {
        private const val DEFAULT_PATTERN = "yyyy-MM-dd HH:mm"

        fun format(millis: Long, pattern: String = DEFAULT_PATTERN): String =
            SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }
}
