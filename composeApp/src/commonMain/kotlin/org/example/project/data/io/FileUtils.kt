package org.example.project.data.io

/**
 * Platform-specific file reading utility.
 * Returns the raw bytes of a file at the given path.
 */
expect suspend fun readFileBytes(filePath: String): ByteArray

/**
 * Returns the file name (basename) from a full file path.
 */
expect fun getFileName(filePath: String): String
