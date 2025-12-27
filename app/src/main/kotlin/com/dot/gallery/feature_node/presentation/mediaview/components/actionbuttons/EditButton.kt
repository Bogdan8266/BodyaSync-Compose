package com.dot.gallery.feature_node.presentation.mediaview.components.actionbuttons

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.dot.gallery.R
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.presentation.util.launchEditIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun <T : Media> EditButton(
    media: T,
    enabled: Boolean,
    followTheme: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Додали scope для асинхронної роботи

    MediaViewButton(
        currentMedia = media,
        imageVector = Icons.Outlined.Edit,
        followTheme = followTheme,
        title = stringResource(R.string.edit),
        enabled = enabled
    ) {
        scope.launch {
            if (media.path.startsWith("http")) {
                // ФІКС: Логіка для серверних файлів
                withContext(Dispatchers.IO) {
                    try {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Downloading for edit...", Toast.LENGTH_SHORT).show()
                        }

                        // 1. Качаємо файл
                        val fileName = "edit_${media.label}" // Додаємо префікс, щоб не плутати
                        val cachePath = File(context.cacheDir, "edited_images")
                        cachePath.mkdirs()
                        val newFile = File(cachePath, fileName)

                        URL(media.path).openStream().use { input ->
                            FileOutputStream(newFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // 2. Отримуємо Content URI
                        val contentUri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            newFile
                        )

                        // 3. Запускаємо Edit Intent
                        val editIntent = Intent(Intent.ACTION_EDIT).apply {
                            setDataAndType(contentUri, media.mimeType)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        }

                        withContext(Dispatchers.Main) {
                            try {
                                context.startActivity(Intent.createChooser(editIntent, "Edit with"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No app found to edit this file", Toast.LENGTH_SHORT).show()
                            }
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Стандартна логіка для локальних файлів
                context.launchEditIntent(it)
            }
        }
    }
}