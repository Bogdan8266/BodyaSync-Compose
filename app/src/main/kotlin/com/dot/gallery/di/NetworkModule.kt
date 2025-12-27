package com.dot.gallery.di

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dot.gallery.core.dataStore
import com.dot.gallery.feature_node.data.remote.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideApiService(@ApplicationContext context: Context): ApiService {
        // 1. Отримуємо збережений IP з DataStore
        // Використовуємо runBlocking, щоб почекати завантаження налаштувань перед запуском мережі
        val savedBaseUrl = runBlocking {
            try {
                context.dataStore.data.map { preferences ->
                    preferences[stringPreferencesKey("server_url")]
                }.first()
            } catch (e: Exception) {
                null
            }
        }

        // 2. Використовуємо збережений або дефолтний (якщо ще не налаштував)
        // Заміни 192.168.31.176 на свій актуальний IP, щоб він був "запасним"
        var baseUrl = savedBaseUrl ?: "http://192.168.31.176:8000/"

        // 3. Retrofit вимагає, щоб URL закінчувався на "/"
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/"
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}