package com.aeliavision.novagrab.core.storage

import java.io.File

object FileUtils {
    fun deleteRecursively(file: File) {
        if (!file.exists()) return
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}
