package com.example.androidbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidbase.designsystem.BaseTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = koinViewModel()
            val session by viewModel.sessionState.collectAsStateWithLifecycle()
            val darkTheme by viewModel.darkTheme.collectAsStateWithLifecycle()
            BaseTheme(darkTheme) {
                val view = LocalView.current
                DisposableEffect(key1 = view, key2 = darkTheme) {
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                    onDispose { }
                }
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    MainGraph(session = session, darkTheme = darkTheme, onSignOut = viewModel::signOut, onThemeSelected = viewModel::selectTheme)
                }
            }
        }
    }
}
