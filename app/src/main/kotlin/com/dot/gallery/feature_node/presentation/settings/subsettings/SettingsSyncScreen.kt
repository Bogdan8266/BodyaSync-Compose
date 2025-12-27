package com.dot.gallery.feature_node.presentation.settings.subsettings

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dot.gallery.core.Position
import com.dot.gallery.core.SettingsEntity
import com.dot.gallery.core.SyncSettings
import com.dot.gallery.feature_node.data.remote.ApiService
import com.dot.gallery.feature_node.data.remote.ServerSettings
// Переконайся, що імпорт SyncService правильний (залежить від того, де ти його створив)
import com.dot.gallery.feature_node.data.service.SyncService
import com.dot.gallery.feature_node.presentation.settings.components.BaseSettingsScreen
import com.dot.gallery.feature_node.presentation.settings.components.rememberPreference
import com.dot.gallery.feature_node.presentation.settings.components.rememberSeekPreference
import com.dot.gallery.feature_node.presentation.settings.components.rememberSwitchPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    fun clearCache(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                apiService.clearCache()
                onResult("Success!")
            } catch (e: Exception) { onResult("Error: ${e.message}") }
        }
    }
    fun generateThumbnails(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                apiService.generateThumbnails()
                onResult("Started!")
            } catch (e: Exception) { onResult("Error: ${e.message}") }
        }
    }
    fun pushSettings(pSize: Int, pQual: Int, tSize: Int, tQual: Int) {
        viewModelScope.launch {
            try { apiService.updateServerSettings(ServerSettings(tSize, tQual, pSize, pQual)) } catch (_: Exception) {}
        }
    }
}

@Composable
fun SettingsSyncScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel = hiltViewModel<SyncViewModel>()

    var showIpDialog by remember { mutableStateOf(false) }

    // --- DataStore Read ---
    val serverUrl = SyncSettings.getServerUrl()
    // ФІКС: Тепер беремо Set<String> (список папок), а не один рядок
    val syncFolders = SyncSettings.getSyncFolders()
    val autoSync = SyncSettings.getAutoSync()

    val photoSize = SyncSettings.getPhotoSize()
    val photoQuality = SyncSettings.getPhotoQuality()
    val thumbSize = SyncSettings.getThumbSize()
    val thumbQuality = SyncSettings.getThumbQuality()

    // --- Folder Picker ---
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            // ФІКС: Викликаємо addSyncFolder
            scope.launch { SyncSettings.addSyncFolder(context, it.toString()) }
        }
    }

    LaunchedEffect(photoSize, photoQuality, thumbSize, thumbQuality) {
        viewModel.pushSettings(photoSize.toInt(), photoQuality.toInt(), thumbSize.toInt(), thumbQuality.toInt())
    }

    @Composable
    fun settingsList(): SnapshotStateList<SettingsEntity> {

        // 1. Connection Section
        val headerConn = SettingsEntity.Header("Connection")
        val ipPref = rememberPreference(
            icon = Icons.Default.Dns,
            title = "Server IP",
            summary = serverUrl,
            onClick = { showIpDialog = true },
            screenPosition = Position.Alone
        )

        // 2. Sync Section (Multi-folder logic)
        val headerSync = SettingsEntity.Header("Sync Folders")

        // Кнопка додавання
        val addFolderPref = rememberPreference(
            icon = Icons.Default.Add,
            title = "Add Folder",
            summary = "Select folder to auto-upload",
            onClick = { folderPicker.launch(null) },
            screenPosition = Position.Top
        )

        // Динамічний список папок (мапимо рядки у Preferences)
        val folderPrefs = syncFolders.map { uriString ->
            // Робимо красиву назву з URI
            val folderName = Uri.parse(uriString).lastPathSegment?.replace("primary:", "") ?: "Folder"

            rememberPreference(
                icon = Icons.Default.Folder,
                title = folderName,
                summary = "Tap to remove",
                onClick = {
                    scope.launch { SyncSettings.removeSyncFolder(context, uriString) }
                },
                screenPosition = Position.Middle
            )
        }

        // Перемикач автозавантаження
        val autoSyncPref = rememberSwitchPreference(
            title = "Auto Sync Service",
            summary = "Run background service",
            isChecked = autoSync,
            onCheck = { isChecked ->
                scope.launch { SyncSettings.setAutoSync(context, isChecked) }
                // ФІКС: Запуск сервісу
                if (isChecked) SyncService.start(context) else SyncService.stop(context)
            },
            screenPosition = Position.Bottom
        )

        // 3. Quality (Full Photo)
        val headerQualFull = SettingsEntity.Header("Quality (Full Photo)")
        val photoSizePref = rememberSeekPreference(
            title = "Max Size (px)",
            currentValue = photoSize,
            minValue = 0f,
            maxValue = 4000f,
            step = 100,
            seekSuffix = "px",
            onSeek = { scope.launch { SyncSettings.setPhotoSize(context, it) } },
            screenPosition = Position.Top
        )
        val photoQualPref = rememberSeekPreference(
            title = "JPEG Quality",
            currentValue = photoQuality,
            minValue = 10f,
            maxValue = 100f,
            step = 5,
            seekSuffix = "%",
            onSeek = { scope.launch { SyncSettings.setPhotoQuality(context, it) } },
            screenPosition = Position.Bottom
        )

        // 4. Quality (Thumbnails)
        val headerQualThumb = SettingsEntity.Header("Quality (Thumbnails)")
        val thumbSizePref = rememberSeekPreference(
            title = "Grid Size",
            currentValue = thumbSize,
            minValue = 100f,
            maxValue = 800f,
            step = 50,
            onSeek = { scope.launch { SyncSettings.setThumbSize(context, it) } },
            screenPosition = Position.Top
        )
        val thumbQualPref = rememberSeekPreference(
            title = "Grid Quality",
            currentValue = thumbQuality,
            minValue = 10f,
            maxValue = 100f,
            onSeek = { scope.launch { SyncSettings.setThumbQuality(context, it) } },
            screenPosition = Position.Bottom
        )

        // 5. Actions
        val headerActions = SettingsEntity.Header("Server Actions")
        val clearCachePref = rememberPreference(
            icon = Icons.Default.DeleteSweep,
            title = "Clear Cache",
            onClick = { viewModel.clearCache { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } },
            screenPosition = Position.Top
        )
        val regenThumbPref = rememberPreference(
            icon = Icons.Default.Refresh,
            title = "Regenerate Thumbs",
            onClick = { viewModel.generateThumbnails { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() } },
            screenPosition = Position.Bottom
        )

        // ЗБИРАЄМО ВСЕ РАЗОМ
        // Важливо передати всі змінні в remember, щоб список оновлювався
        return remember(
            serverUrl, syncFolders, autoSync,
            photoSize, photoQuality, thumbSize, thumbQuality
        ) {
            mutableStateListOf(
                headerConn, ipPref,

                headerSync, addFolderPref,
                *folderPrefs.toTypedArray(), // Додаємо динамічний список папок
                autoSyncPref,

                headerQualFull, photoSizePref, photoQualPref,
                headerQualThumb, thumbSizePref, thumbQualPref,
                headerActions, clearCachePref, regenThumbPref
            )
        }
    }

    BaseSettingsScreen(title = "Synchronization", settingsList = settingsList())

    if (showIpDialog) {
        var tempIp by remember { mutableStateOf(serverUrl) }
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Server IP") },
            text = { OutlinedTextField(value = tempIp, onValueChange = { tempIp = it }) },
            confirmButton = {
                Button(onClick = {
                    scope.launch { SyncSettings.setServerUrl(context, tempIp) }
                    showIpDialog = false
                    Toast.makeText(context, "Restart App!", Toast.LENGTH_LONG).show()
                }) { Text("Save") }
            },
            dismissButton = { Button(onClick = { showIpDialog = false }) { Text("Cancel") } }
        )
    }
}