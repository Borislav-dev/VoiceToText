package org.example.project.domain.share

import android.content.Context
import android.content.Intent

actual class ShareManager actual constructor(context: Any?) : IShareManager {
    private val appContext: Context? = context as? Context

    override fun shareText(title: String, text: String) {
        if (appContext == null) return
        
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        
        val shareIntent = Intent.createChooser(sendIntent, "Share note via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(shareIntent)
    }
}
