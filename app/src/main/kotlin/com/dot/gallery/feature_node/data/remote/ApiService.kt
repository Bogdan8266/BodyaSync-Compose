package com.dot.gallery.feature_node.data.remote

import retrofit2.http.GET
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import androidx.annotation.Keep
// GET вже там має бути
interface ApiService {
    @GET("gallery/")
    suspend fun getGallery(): List<ServerGalleryItem>

    @POST("thumbnails/clear_cache/")
    suspend fun clearCache(): StatusResponse

    @POST("thumbnails/generate_all/")
    suspend fun generateThumbnails(): StatusResponse

    @POST("settings/")
    suspend fun updateServerSettings(@Body settings: ServerSettings): StatusResponse

    @Multipart
    @POST("upload/") // Або "files/upload_to_path/" якщо хочеш в конкретну папку
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): StatusResponse
}


data class StatusResponse(val status: String, val message: String? = null)

data class ServerSettings(
    val preview_size: Int,
    val preview_quality: Int,
    val photo_size: Int,
    val photo_quality: Int
)
@Keep
data class ServerGalleryItem(
    val filename: String,
    val type: String,
    val thumbnail: String,
    val timestamp: Double?
)