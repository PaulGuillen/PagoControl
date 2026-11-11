package com.devpaul.pagocontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.domain.AppearanceRepository
import com.devpaul.pagocontrol.domain.ThemeMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val appearance: AppearanceRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val theme by appearance.themeMode.collectAsStateWithLifecycle(initialValue = null)
            var themeError by rememberSaveable { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            val darkTheme = when (theme) {
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
                ThemeMode.System, null -> isSystemInDarkTheme()
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            PagoControlTheme(darkTheme = darkTheme) {
                Surface(Modifier.fillMaxSize()) {
                    if (theme == null) {
                        Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    } else {
                        MainGraph(
                            navController = navController,
                            themeIndex = theme?.ordinal ?: 0,
                            onThemeSelected = { index ->
                                ThemeMode.entries.getOrNull(index)?.let { mode ->
                                    scope.launch {
                                        try {
                                            appearance.setThemeMode(mode)
                                        } catch (error: CancellationException) {
                                            throw error
                                        } catch (_: Exception) {
                                            themeError = true
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
                if (themeError) {
                    AlertDialog(
                        onDismissRequest = { themeError = false },
                        text = { Text(stringResource(R.string.theme_save_error)) },
                        confirmButton = {
                            TextButton(onClick = { themeError = false }) {
                                Text(stringResource(R.string.accept))
                            }
                        },
                    )
                }
            }
        }
    }
}
