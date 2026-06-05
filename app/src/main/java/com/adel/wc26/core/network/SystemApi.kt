package com.adel.wc26.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class SystemStatusDto(
    @SerialName("min_android_version") val minAndroidVersion: Int,
    @SerialName("maintenance_mode") val maintenanceMode: Boolean,
    @SerialName("android_update_url") val androidUpdateUrl: String
)

@Serializable
data class ForceUpdateErrorDto(
    val error: String,
    @SerialName("android_update_url") val androidUpdateUrl: String,
    @SerialName("min_android_version") val minAndroidVersion: Int
)

@Serializable
data class MaintenanceErrorDto(
    val error: String,
    @SerialName("maintenance_mode") val maintenanceMode: Boolean
)

interface SystemApi {
    @GET("system-status")
    suspend fun getSystemStatus(): SystemStatusDto
}