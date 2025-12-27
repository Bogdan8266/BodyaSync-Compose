package com.dot.gallery.feature_node.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.dot.gallery.R
import com.dot.gallery.core.dataStore
import com.dot.gallery.feature_node.data.remote.ApiService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// 1. Додаємо цю анотацію, щоб Hilt знав, як працювати з цим сервісом
@AndroidEntryPoint
class SyncService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // 2. Інжектимо ApiService автоматично (Hilt сам розбереться з IP)
    @Inject
    lateinit var apiService: ApiService

    private val uploadedCache = mutableSetOf<Long>()

    private val contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d("BodyaSync", "📸 Зміни в галереї! Запускаю сканування...")
            scanAndUpload()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // Тут більше не треба створювати apiService вручну, Hilt це зробив за нас

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Sync Service Active", 0, false))

        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )
        contentResolver.registerContentObserver(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserver
        )

        scanAndUpload()
    }

    private fun scanAndUpload() {
        serviceScope.launch {
            val folderUris = applicationContext.dataStore.data.map {
                it[stringSetPreferencesKey("sync_folders_set")] ?: emptySet()
            }.first()

            if (folderUris.isEmpty()) {
                Log.d("BodyaSync", "⚠️ Папки для синхронізації не вибрані")
                return@launch
            }

            val targetPaths = folderUris.mapNotNull { uriString ->
                Uri.parse(uriString).lastPathSegment?.replace("primary:", "")
            }
            Log.d("BodyaSync", "📂 Відстежуємо папки: $targetPaths")

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME
            )

            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val cursor = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, sortOrder
            )

            cursor?.use {
                val idCol = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val pathCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val nameCol = it.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

                var processedCount = 0
                val limit = 10

                while (it.moveToNext()) {
                    if (processedCount >= limit) break

                    val id = it.getLong(idCol)
                    val filePath = it.getString(pathCol)
                    val name = it.getString(nameCol)

                    if (uploadedCache.contains(id)) continue

                    val isTarget = targetPaths.any { folder -> filePath.contains(folder) }

                    if (isTarget) {
                        Log.d("BodyaSync", "🚀 Знайдено новий файл: $name")
                        val contentUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())

                        uploadFile(contentUri, name)
                        uploadedCache.add(id)
                        processedCount++
                    }
                }
            }
        }
    }

    private suspend fun uploadFile(uri: Uri, fileName: String) {
        val file = File(cacheDir, "temp_upload")
        try {
            updateNotification("Uploading $fileName...", 0, true)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }

            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

            // Використовуємо injected змінну
            apiService.uploadFile(body)

            Log.d("BodyaSync", "✅ Успішно завантажено: $fileName")
            updateNotification("Uploaded: $fileName", 100, false)
            delay(1000)
            updateNotification("Sync Active: Waiting for changes...", 0, false)

        } catch (e: Exception) {
            Log.e("BodyaSync", "❌ Помилка завантаження $fileName: ${e.message}")
            updateNotification("Error: ${e.message}", 0, false)
        } finally {
            if (file.exists()) file.delete()
        }
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "sync_channel"
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, "Background Sync", NotificationManager.IMPORTANCE_LOW)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(text: String, progress: Int, indeterminate: Boolean): Notification {
        val channelId = "sync_channel"
        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("BodyaSync")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setOnlyAlertOnce(true)

        if (indeterminate || progress > 0) {
            builder.setProgress(100, progress, indeterminate)
        }
        return builder.build()
    }

    private fun updateNotification(text: String, progress: Int, indeterminate: Boolean) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, createNotification(text, progress, indeterminate))
    }

    override fun onDestroy() {
        contentResolver.unregisterContentObserver(contentObserver)
        serviceJob.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val NOTIFICATION_ID = 999

        fun start(context: Context) {
            val intent = Intent(context, SyncService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SyncService::class.java))
        }
    }
}