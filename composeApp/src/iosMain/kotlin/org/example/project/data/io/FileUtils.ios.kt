package org.example.project.data.io

actual suspend fun readFileBytes(filePath: String): ByteArray {
    throw UnsupportedOperationException("File reading not yet implemented for iOS")
}

actual fun getFileName(filePath: String): String {
    return filePath.substringAfterLast("/")
}
