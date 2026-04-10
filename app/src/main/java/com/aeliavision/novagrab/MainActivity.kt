package com.aeliavision.novagrab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.aeliavision.novagrab.core.ui.navigation.AppNavHost
import com.aeliavision.novagrab.ui.theme.NovaGrabTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val _openUrlEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )

        handleDeepLinkIntent(intent)

        setContent {
            NovaGrabTheme {
                AppNavHost(openUrlEvents = _openUrlEvents.asSharedFlow())
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val data: Uri = intent?.data ?: return
        if (intent.action != Intent.ACTION_VIEW) return
        if (data.scheme != "smartvdl") return
        if (data.host != "open") return

        val url = data.getQueryParameter("url")
            ?: data.getQueryParameter("u")
            ?: data.getQueryParameter("link")
            ?: return

        lifecycleScope.launch {
            _openUrlEvents.emit(url)
        }
    }
}
