package com.dot.gallery.core

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
// Імпортуємо dataStore (він має бути доступний в цьому пакеті)
import com.dot.gallery.core.dataStore

object SyncSettings {
    // Ключі налаштувань
    private val SERVER_URL = stringPreferencesKey("server_url")
    private val SYNC_FOLDER_URI = stringPreferencesKey("sync_folder_uri")
    private val AUTO_SYNC = booleanPreferencesKey("auto_sync")
    private val DELETE_AFTER_SYNC = booleanPreferencesKey("delete_after_sync")

    private val PHOTO_SIZE = floatPreferencesKey("server_photo_size")
    private val PHOTO_QUALITY = floatPreferencesKey("server_photo_quality")
    private val THUMB_SIZE = floatPreferencesKey("server_thumb_size")
    private val THUMB_QUALITY = floatPreferencesKey("server_thumb_quality")

    private val SYNC_FOLDERS = androidx.datastore.preferences.core.stringSetPreferencesKey("sync_folders_set")

    // --- Власні помічники (щоб не залежати від інших файлів) ---

    private fun <T> Context.getSettingFlow(key: Preferences.Key<T>, defaultValue: T): Flow<T> {
        return this.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    private suspend fun <T> Context.setSettingValue(key: Preferences.Key<T>, value: T) {
        this.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    // --- Читання (використовуємо в @Composable) ---

    @Composable
    fun getServerUrl(): String =
        LocalContext.current.getSettingFlow(SERVER_URL, "http://192.168.31.176:8000")
            .collectAsState(initial = "http://192.168.31.176:8000").value

  //  @Composable
  //  fun getSyncFolder(): String =
   //     LocalContext.current.getSettingFlow(SYNC_FOLDER_URI, "")
   //         .collectAsState(initial = "").value

    @Composable
    fun getAutoSync(): Boolean =
        LocalContext.current.getSettingFlow(AUTO_SYNC, false)
            .collectAsState(initial = false).value

    @Composable
    fun getDeleteAfterSync(): Boolean =
        LocalContext.current.getSettingFlow(DELETE_AFTER_SYNC, false)
            .collectAsState(initial = false).value

    @Composable
    fun getPhotoSize(): Float =
        LocalContext.current.getSettingFlow(PHOTO_SIZE, 0f)
            .collectAsState(initial = 0f).value

    @Composable
    fun getPhotoQuality(): Float =
        LocalContext.current.getSettingFlow(PHOTO_QUALITY, 100f)
            .collectAsState(initial = 100f).value

    @Composable
    fun getThumbSize(): Float =
        LocalContext.current.getSettingFlow(THUMB_SIZE, 400f)
            .collectAsState(initial = 400f).value

    @Composable
    fun getThumbQuality(): Float =
        LocalContext.current.getSettingFlow(THUMB_QUALITY, 80f)
            .collectAsState(initial = 80f).value

    // --- Запис (використовуємо в CoroutineScope) ---
    @Composable
    fun getSyncFolders(): Set<String> =
        LocalContext.current.getSettingFlow(SYNC_FOLDERS, emptySet())
            .collectAsState(initial = emptySet()).value

    suspend fun addSyncFolder(context: Context, uri: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SYNC_FOLDERS] ?: emptySet()
            prefs[SYNC_FOLDERS] = current + uri
        }
    }

    suspend fun removeSyncFolder(context: Context, uri: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[SYNC_FOLDERS] ?: emptySet()
            prefs[SYNC_FOLDERS] = current - uri
        }
    }
    suspend fun setServerUrl(context: Context, value: String) =
        context.setSettingValue(SERVER_URL, value)

   // suspend fun setSyncFolder(context: Context, value: String) =
      //  context.setSettingValue(SYNC_FOLDER_URI, value)

    suspend fun setAutoSync(context: Context, value: Boolean) =
        context.setSettingValue(AUTO_SYNC, value)

    suspend fun setDeleteAfterSync(context: Context, value: Boolean) =
        context.setSettingValue(DELETE_AFTER_SYNC, value)

    suspend fun setPhotoSize(context: Context, value: Float) =
        context.setSettingValue(PHOTO_SIZE, value)

    suspend fun setPhotoQuality(context: Context, value: Float) =
        context.setSettingValue(PHOTO_QUALITY, value)

    suspend fun setThumbSize(context: Context, value: Float) =
        context.setSettingValue(THUMB_SIZE, value)

    suspend fun setThumbQuality(context: Context, value: Float) =
        context.setSettingValue(THUMB_QUALITY, value)
}