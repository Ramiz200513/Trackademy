package org.example.project.domain.models

data class Material(
    val id: String,
    val title: String,
    val description: String?,
    val originalFileName: String,
    val contentType: String,
    val fileSize: Long,
    val groupId: String?,
    val groupName: String?,
    val uploadedByName: String?,
    val createdAt: String?
) {
    val fileSizeKb: Long get() = fileSize / 1024
    val fileExtension: String get() = originalFileName.substringAfterLast('.', "")
    val isImage: Boolean get() = contentType.startsWith("image/")
    val isPdf: Boolean get() = contentType == "application/pdf"
    val isDocument: Boolean get() = contentType.contains("word") || contentType.contains("document")
}

