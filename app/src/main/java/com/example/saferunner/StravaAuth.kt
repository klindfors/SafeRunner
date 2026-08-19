package com.example.saferunner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import com.squareup.moshi.Moshi

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

    suspend fun exchangeCodeForToken(code: String): StravaToken? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://www.strava.com/oauth/token")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"
                )

                val body = "client_id=$clientId" +
                        "&client_secret=$clientSecret" +
                        "&code=$code" +
                        "&grant_type=authorization_code"

                connection.outputStream.use { output ->
                    output.write(body.toByteArray())
                }

                val responseCode = connection.responseCode

                Log.d("SafeRunner", "Token response code: $responseCode")

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream
                        .bufferedReader()
                        .use { it.readText() }

                    Log.d("SafeRunner", "Token response: received")

                    val moshi = Moshi.Builder().build()
                    val adapter = moshi.adapter(StravaToken::class.java)

                    return@withContext adapter.fromJson(response)
                } else {
                    val error = connection.errorStream
                        ?.bufferedReader()
                        ?.use { it.readText() }

                    Log.e("SafeRunner", "Token exchange failed: $error")
                    null
                }

            } catch (e: Exception) {
                Log.e("SafeRunner", "Token exchange error", e)
                null
            }
        }
    }
}
