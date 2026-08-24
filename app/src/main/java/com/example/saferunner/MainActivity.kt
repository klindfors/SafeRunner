package com.example.saferunner

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.lifecycle.lifecycleScope
import com.example.saferunner.data.StravaAuth
import com.example.saferunner.data.StravaTokenStorage
import com.example.saferunner.ui.LoginScreen
import com.example.saferunner.ui.theme.SafeRunnerTheme
import kotlinx.coroutines.launch

private var stravaConnected by mutableStateOf(false)


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        //handle intent if the app was started via a deep link
        handleIntent(intent)

        setContent {
            SafeRunnerTheme {
                LoginScreen(
                    stravaConnected = stravaConnected,
                    onConnectClick = {
                        StravaAuth.openAuthPage(this)
                    }
                )

            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        //extra logging
        Log.d("SafeRunner", "handleIntent() called")
        Log.d("SafeRunner", "Intent: $intent")
        Log.d("SafeRunner", "Intent data: ${intent?.data}")
        intent?.data?.let { uri ->
            if (uri.scheme == "saferunner" && uri.host == "callback") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    Log.d("SafeRunner", "Got auth code: $code")

                    lifecycleScope.launch {
                        val token = StravaAuth.exchangeCodeForToken(code)

                        if (token != null) {
                            StravaTokenStorage.save(this@MainActivity, token)
                            stravaConnected = true
                            Log.d("SafeRunner", "Strava token saved successfully")
                        } else {
                            Log.e("SafeRunner", "Failed to parse Strava token")
                        }
                    }}else {
                        Log.e("SafeRunner", "No authorization code received") }
            }
        }
    }
}

@PreviewScreenSizes
@Composable
fun SafeRunnerApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    FAVORITES("Favorites", R.drawable.ic_favorite),
    PROFILE("Profile", R.drawable.ic_account_box),
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SafeRunnerTheme {
        Greeting("Android")
    }
}