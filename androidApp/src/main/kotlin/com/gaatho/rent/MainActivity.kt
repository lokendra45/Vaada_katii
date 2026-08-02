package com.gaatho.rent

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import com.gaatho.rent.core.utils.ActivityProvider

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityProvider.activity = this
        enableEdgeToEdge()
        setContent {
            App()
        }
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