package org.example.project.domain.utils

interface FileSaver {
    fun saveFile(bytes: ByteArray, fileName: String)
}