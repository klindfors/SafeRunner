package com.example.saferunner.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StravaToken(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "expires_at") val expiresAt: Long,
    @Json(name = "token_type") val tokenType: String
)