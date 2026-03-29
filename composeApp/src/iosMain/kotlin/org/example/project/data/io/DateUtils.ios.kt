package org.example.project.data.io

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual fun formatTimestamp(millis: Long): String {
    val date = NSDate(timeIntervalSince1970 = millis / 1000.0)
    val formatter = NSDateFormatter().apply {
        dateFormat = "dd MMM yyyy, HH:mm"
    }
    return formatter.stringFromDate(date)
}
