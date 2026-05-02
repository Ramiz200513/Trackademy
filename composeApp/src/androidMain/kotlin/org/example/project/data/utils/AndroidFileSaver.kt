package org.example.project.data.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.example.project.domain.utils.FileSaver
import java.io.File
import java.io.FileOutputStream

class AndroidFileSaver(private val context: Context) : FileSaver {

    companion object {
        private const val CHANNEL_ID = "file_downloads"
        private const val CHANNEL_NAME = "Загрузки файлов"
    }

    init {
        createNotificationChannel()
    }

    override fun saveFile(bytes: ByteArray, fileName: String) {
        try {
            println("💾 ПЫТАЕМСЯ СОХРАНИТЬ ФАЙЛ: $fileName, размер: ${bytes.size} байт")

            var fileUri: Uri? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(fileName))
                }

                fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (fileUri != null) {
                    resolver.openOutputStream(fileUri)?.use { outputStream ->
                        outputStream.write(bytes)
                        println("🎉 ФАЙЛ УСПЕШНО СОХРАНЕН ЧЕРЕЗ MediaStore!")
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()

                val file = File(downloadsDir, fileName)
                FileOutputStream(file).use { it.write(bytes) }
                fileUri = Uri.fromFile(file)
                println("🎉 ФАЙЛ УСПЕШНО СОХРАНЕН СТАРЫМ СПОСОБОМ!")
            }

            showDownloadNotification(fileName, fileUri)

        } catch (e: Exception) {
            println("FILE SAVE ERROR: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun showDownloadNotification(fileName: String, fileUri: Uri?) {
        val notificationId = System.currentTimeMillis().toInt()

        // Intent для открытия файла при нажатии на уведомление
        val openIntent = if (fileUri != null) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(fileUri, getMimeType(fileName))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Файл сохранён")
            .setContentText(fileName)
            .setSubText("Загрузки")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .apply {
                if (openIntent != null) {
                    setContentIntent(openIntent)
                    addAction(
                        android.R.drawable.ic_menu_view,
                        "Открыть",
                        openIntent
                    )
                }
            }
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Пользователь не дал разрешение на уведомления (Android 13+)
            println("NOTIFICATION PERMISSION DENIED: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о сохранённых файлах"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "gif"         -> "image/gif"
            "webp"        -> "image/webp"
            "pdf"         -> "application/pdf"
            "doc"         -> "application/msword"
            "docx"        -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            "xls"         -> "application/vnd.ms-excel"
            "xlsx"        -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            "ppt"         -> "application/vnd.ms-powerpoint"
            "pptx"        -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
            "txt"         -> "text/plain"
            "zip"         -> "application/zip"
            "mp4"         -> "video/mp4"
            "mp3"         -> "audio/mpeg"
            else          -> "application/octet-stream"
        }
    }
}