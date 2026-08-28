package com.gaatho.rent.core.utils

import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun savePdfFile(bytes: ByteArray, fileName: String): Boolean = withContext(Dispatchers.IO) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { output ->
            output.write(bytes)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
