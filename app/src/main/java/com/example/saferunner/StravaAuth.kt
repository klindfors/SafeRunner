package com.example.saferunner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object StravaAuth {

    private const val TAG = "SafeRunner"
    private val clientId = BuildConfig.STRAVA_CLIENT_ID
    private val clientSecret = BuildConfig.STRAVA_CLIENT_SECRET
    private const val REDIRECT_URI = "https://kimlindfors.se/callback"
    private const val SCOPE = "activity:read_all"

    fun getAuthUrl(): String {
        return Uri.parse("https://www.strava.com/oauth/mobile/authorize")
            .buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("approval_prompt", "auto")
            .appendQueryParameter("scope", SCOPE)
            .build()
            .toString()
    }

    fun openAuthPage(context: Context) {
        val url = getAuthUrl()
        Log.d(TAG, "Opening URL: $url")
        
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            //ensuring that we may start the activity from a non-activity context if needed
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        try {
            context.startActivity(intent)
            Log.d(TAG, "Activity started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening browser: ${e.message}")
        }
    }
}
