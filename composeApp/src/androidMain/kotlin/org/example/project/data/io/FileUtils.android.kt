package org.example.project.data.io

import java.io.File

actual suspend fun readFileBytes(filePath: String): ByteArray {
    return File(filePath).readBytes()
}

actual fun getFileName(filePath: String): String {
    return File(filePath).name
}
