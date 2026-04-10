package com.example.saferunner.com.example.saferunner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.saferunner.BuildConfig

object StravaAuth {

    val clientId = BuildConfig.STRAVA_CLIENT_ID
    val clientSecret = BuildConfig.STRAVA_CLIENT_SECRET
    val redirectUri = "https://saferunner.app/callback"
    private const val SCOPE = "activity:read_all"

    fun getAuthUrl(): String {
        return "https://www.strava.com/oauth/mobile/authorize" +
                "?client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&response_type=code" +
                "&approval_prompt=auto" +
                "&scope=activity:read_all"
    }

    fun openAuthPage(context: Context) {
        val url = getAuthUrl()
        Log.d("SafeRunner", "Opening URL: $url")
        val uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(intent)
            Log.d("SafeRunner", "Activity started successfully")
        } catch (e: Exception) {
            Log.e("SafeRunner", "Error opening browser: ${e.message}")
        }

    }
}