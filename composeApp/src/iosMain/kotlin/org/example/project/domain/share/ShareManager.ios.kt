package org.example.project.domain.share

actual class ShareManager actual constructor(context: Any?) : IShareManager {
    override fun shareText(title: String, text: String) {
        // iOS share sheet stub. Will be implemented natively on iOS using UIActivityViewController.
    }
}
