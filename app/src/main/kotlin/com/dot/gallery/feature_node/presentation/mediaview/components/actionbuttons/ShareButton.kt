package com.dot.gallery.feature_node.presentation.mediaview.components.actionbuttons

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.dot.gallery.R
import com.dot.gallery.feature_node.data.data_source.KeychainHolder
import com.dot.gallery.feature_node.domain.model.Media
import com.dot.gallery.feature_node.domain.model.Vault
import com.dot.gallery.feature_node.domain.util.isEncrypted
import com.dot.gallery.feature_node.presentation.util.shareEncryptedMedia
import com.dot.gallery.feature_node.presentation.util.shareMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@Composable
fun <T : Media> ShareButton(
    media: T,
    enabled: Boolean,
    followTheme: Boolean = false,
    currentVault: Vault? = null
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    MediaViewButton(
        currentMedia = media,
        imageVector = Icons.Outlined.Share,
        followTheme = followTheme,
        title = stringResource(R.string.share),
        enabled = enabled
    ) {
        scope.launch {
            // ФІКС: Перевіряємо, чи це серверний файл
            if (media.path.startsWith("http")) {
                withContext(Dispatchers.IO) {
                    try {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Downloading for share...", Toast.LENGTH_SHORT).show()
                        }

                        // 1. Створюємо тимчасовий файл
                        val fileName = media.label
                        val cachePath = File(context.cacheDir, "shared_images")
                        cachePath.mkdirs()
                        val newFile = File(cachePath, fileName)

                        // 2. Качаємо з сервера (media.path - це посилання на оригінал)
                        URL(media.path).openStream().use { input ->
                            FileOutputStream(newFile).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // 3. Отримуємо URI через FileProvider (безепечний доступ для інших програм)
                        val contentUri: Uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            newFile
                        )

                        // 4. Запускаємо стандартний Share Intent
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = media.mimeType
                            putExtra(Intent.EXTRA_STREAM, contentUri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        withContext(Dispatchers.Main) {
                            context.startActivity(Intent.createChooser(shareIntent, "Share Media"))
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else if (media.isEncrypted && currentVault != null) {
                // Share encrypted media by decrypting it first
                val keychainHolder = KeychainHolder(context)
                context.shareEncryptedMedia(
                    media = it,
                    vault = currentVault,
                    keychainHolder = keychainHolder
                )
            } else {
                // Share regular local media normally
                context.shareMedia(media = it)
            }
        }
    }
}