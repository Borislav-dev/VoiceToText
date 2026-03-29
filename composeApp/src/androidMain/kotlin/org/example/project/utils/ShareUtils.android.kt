package org.example.project.utils

import android.content.Context
import android.content.Intent

actual fun shareText(context: Any?, text: String) {
    val ctx = context as? Context ?: return
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Share via")
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    ctx.startActivity(shareIntent)
}
