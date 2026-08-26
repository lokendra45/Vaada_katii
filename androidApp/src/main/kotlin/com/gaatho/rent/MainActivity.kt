package com.gaatho.rent

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import com.gaatho.rent.core.auth.AuthDeepLinkFlags
import com.gaatho.rent.core.utils.ActivityProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.android.ext.android.inject

class MainActivity : FragmentActivity() {

    private val supabase: SupabaseClient by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleRedirect(intent)
        ActivityProvider.activity = this
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides this) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleRedirect(intent)
    }

    private fun handleRedirect(intent: Intent) {
        // Mark that an OAuth redirect is in flight so the Splash screen can wait for the
        // PKCE code exchange to finish before routing to Login.
        if (isOAuthRedirect(intent)) {
            AuthDeepLinkFlags.pendingOAuth = true
        }
        supabase.handleDeeplinks(
            intent = intent,
            onError = { e ->
                Log.e("AuthDeepLink", "OAuth deep link exchange failed", e)
                runOnUiThread {
                    Toast.makeText(
                        this@MainActivity,
                        "Sign-in failed. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    private fun isOAuthRedirect(intent: Intent?): Boolean {
        val data = intent?.data ?: return false
        return data.scheme == "com.gaatho.rent" && data.host == "login-callback"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ActivityProvider.activity == this) {
            ActivityProvider.activity = null
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}