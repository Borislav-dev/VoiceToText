package org.example.project.data.io

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun formatTimestamp(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(millis))
}
