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

@AndroidEntryPoint
class SyncService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

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
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Sync Service Active", 0, false))

        // Слідкуємо і за ФОТО, і за ВІДЕО
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

            if (folderUris.isEmpty()) return@launch

            val targetPaths = folderUris.mapNotNull { uriString ->
                Uri.parse(uriString).lastPathSegment?.replace("primary:", "")
            }

            // 1. СКАНУЄМО ФОТО
            scanCollection(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATE_ADDED,
                targetPaths
            )

            // 2. СКАНУЄМО ВІДЕО (Цього не вистачало!)
            scanCollection(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.DATE_ADDED,
                targetPaths
            )
        }
    }

    // Універсальна функція для сканування (щоб не дублювати код)
    private suspend fun scanCollection(collectionUri: Uri, dateColumn: String, targetPaths: List<String>) {
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.DISPLAY_NAME
        )

        val sortOrder = "$dateColumn DESC"

        val cursor = contentResolver.query(
            collectionUri,
            projection, null, null, sortOrder
        )

        cursor?.use {
            val idCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val pathCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val nameCol = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)

            var processedCount = 0
            val limit = 5 // Перевіряємо по 5 останніх фото і 5 відео

            while (it.moveToNext()) {
                if (processedCount >= limit) break

                val id = it.getLong(idCol)
                val filePath = it.getString(pathCol)
                val name = it.getString(nameCol)

                // Унікальний ключ для кешу (щоб не плутати ID фото і відео)
                // Додаємо префікс типу контенту до ID, наприклад "123_video"
                val uniqueId = id + collectionUri.hashCode()

                if (uploadedCache.contains(uniqueId)) continue

                val isTarget = targetPaths.any { folder -> filePath.contains(folder) }

                if (isTarget) {
                    Log.d("BodyaSync", "🚀 Знайдено новий файл: $name")
                    val contentUri = Uri.withAppendedPath(collectionUri, id.toString())

                    uploadFile(contentUri, name, filePath)
                    uploadedCache.add(uniqueId)
                    processedCount++
                }
            }
        }
    }

    private suspend fun uploadFile(uri: Uri, fileName: String, physicalPath: String) {
        val tempFile = File(cacheDir, "temp_upload")
        try {
            updateNotification("Uploading $fileName...", 0, true)

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output -> input.copyTo(output) }
            }

            val requestFile = tempFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", fileName, requestFile)

            val response = apiService.uploadFile(body)

            if (response.status == "success") {
                Log.d("BodyaSync", "✅ Успішно завантажено: $fileName")

                // ПЕРЕВІРКА: Чи треба видаляти?
                val shouldDelete = applicationContext.dataStore.data.map {
                    it[androidx.datastore.preferences.core.booleanPreferencesKey("delete_after_sync")] ?: false
                }.first()

                if (shouldDelete) {
                    // ВИДАЛЕННЯ "ГРУБОЮ СИЛОЮ"
                    val fileToDelete = File(physicalPath)
                    if (fileToDelete.exists()) {
                        val isDeleted = fileToDelete.delete()
                        if (isDeleted) {
                            Log.d("BodyaSync", "🗑️ Файл фізично видалено: $physicalPath")

                            // Оновлюємо MediaStore, щоб порожня іконка не висіла в галереї
                            try {
                                contentResolver.delete(uri, null, null)
                            } catch (e: Exception) {
                                // Ігноруємо помилку оновлення бази, якщо файл вже видалений
                            }
                        } else {
                            Log.e("BodyaSync", "❌ Не вдалося видалити файл фізично (можливо, немає дозволу MANAGE_EXTERNAL_STORAGE)")
                        }
                    }
                }

                updateNotification("Uploaded: $fileName", 100, false)
                delay(1000)
                updateNotification("Sync Active: Waiting...", 0, false)
            }

        } catch (e: Exception) {
            Log.e("BodyaSync", "❌ Помилка завантаження $fileName: ${e.message}")
            updateNotification("Error: ${e.message}", 0, false)
        } finally {
            if (tempFile.exists()) tempFile.delete()
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