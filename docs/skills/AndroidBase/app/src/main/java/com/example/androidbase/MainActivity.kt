package com.example.androidbase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
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
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
                    MainGraph(session, darkTheme, viewModel::signOut)
                }
            }
        }
    }
}
