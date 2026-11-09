package com.devpaul.pagocontrol

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.domain.ThemeMode
import org.koin.androidx.compose.koinViewModel

@Composable
fun PagoControlApp(viewModel: AppViewModel = koinViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val themeError by viewModel.themeError.collectAsStateWithLifecycle()
    val darkTheme = when (theme) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System, null -> isSystemInDarkTheme()
    }
    PagoControlTheme(darkTheme) {
        val view = LocalView.current
        DisposableEffect(view, darkTheme) {
            val window = (view.context as? android.app.Activity)?.window
            window?.let {
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            onDispose { }
        }
        if (theme == null) {
            Surface(Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            }
        } else {
            MainGraph(session, theme?.ordinal ?: 0, viewModel::selectTheme)
        }
        if (themeError) {
            AlertDialog(
                onDismissRequest = viewModel::dismissThemeError,
                text = { Text(stringResource(R.string.theme_save_error)) },
                confirmButton = { TextButton(onClick = viewModel::dismissThemeError) { Text(stringResource(R.string.accept)) } },
            )
        }
    }
}
