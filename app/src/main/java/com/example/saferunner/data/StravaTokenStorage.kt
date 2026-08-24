package com.example.saferunner.data

import android.content.Context
import androidx.core.content.edit

object StravaTokenStorage {

    private const val PREFS_NAME = "strava_auth"

    private const val ACCESS_TOKEN = "access_token"
    private const val REFRESH_TOKEN = "refresh_token"
    private const val EXPIRES_AT = "expires_at"
    private const val TOKEN_TYPE = "token_type"

    fun save(context: Context, token: StravaToken) {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        preferences.edit()
            .putString(ACCESS_TOKEN, token.accessToken)
            .putString(REFRESH_TOKEN, token.refreshToken)
            .putLong(EXPIRES_AT, token.expiresAt)
            .putString(TOKEN_TYPE, token.tokenType)
            .apply()
    }

    fun load(context: Context): StravaToken? {
        val preferences = context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

        val accessToken = preferences.getString(ACCESS_TOKEN, null)
        val refreshToken = preferences.getString(REFRESH_TOKEN, null)
        val tokenType = preferences.getString(TOKEN_TYPE, null)

        if (accessToken == null ||
            refreshToken == null ||
            tokenType == null
        ) {
            return null
        }

        return StravaToken(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = preferences.getLong(EXPIRES_AT, 0L),
            tokenType = tokenType
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        ).edit {
            clear()
        }
    }
}