package com.devpaul.pagocontrol

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.devpaul.pagocontrol.designsystem.EntryColors
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.ForgotPasswordScreen
import com.devpaul.pagocontrol.features.auth.ui.forgotpassword.ForgotPasswordUiState
import com.devpaul.pagocontrol.features.auth.ui.login.LoginScreen
import com.devpaul.pagocontrol.features.auth.ui.login.LoginUiState
import com.devpaul.pagocontrol.features.auth.ui.register.RegisterScreen
import com.devpaul.pagocontrol.features.auth.ui.register.RegisterUiState
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthAppearanceTest {
    @get:Rule val compose = createComposeRule()

    @Test fun loginDark() = captureLogin(true)

    @Test fun loginLight() = captureLogin(false)

    @Test fun registerDark() = captureRegister(true)

    @Test fun registerLight() = captureRegister(false)

    @Test fun recoveryDark() = captureRecovery(true)

    @Test fun recoveryLight() = captureRecovery(false)

    @Test fun providerButtonsKeepTheirInformationDialogs() {
        setContent(false) { LoginScreen(state = LoginUiState(), onIntent = {}, themeIndex = 1, onThemeSelected = {}) }
        compose.onNodeWithText("Continuar con Google").performScrollTo().performClick()
        compose.onNodeWithText("Entendido").performClick()
        compose.onNodeWithText("Ingresar con huella digital").performScrollTo().performClick()
        compose.onNodeWithText("Entendido").performClick()
    }

    private fun captureLogin(dark: Boolean) {
        setContent(dark) {
            LoginScreen(state = LoginUiState(), onIntent = {}, themeIndex = if (dark) 2 else 1, onThemeSelected = {})
        }
        saveImage("login-${if (dark) "dark" else "light"}-top")
        val footer = "¿No tienes cuenta? Regístrate aquí"
        compose.onNodeWithText(footer).performScrollTo()
        assertLinkStyle(text = footer, action = "Regístrate aquí", dark = dark)
        saveImage("login-${if (dark) "dark" else "light"}-footer")
    }

    private fun captureRegister(dark: Boolean) {
        setContent(dark) { RegisterScreen(state = RegisterUiState(), onIntent = {}, onLogin = {}) }
        val footer = "¿Ya tienes una cuenta? Iniciar sesión"
        compose.onNodeWithText(footer).performScrollTo()
        assertLinkStyle(text = footer, action = "Iniciar sesión", dark = dark)
        saveImage("register-${if (dark) "dark" else "light"}-footer")
    }

    private fun captureRecovery(dark: Boolean) {
        setContent(dark) { ForgotPasswordScreen(state = ForgotPasswordUiState(), onIntent = {}, onLogin = {}) }
        compose.onNodeWithText("Recuperar contraseña").assertDoesNotExist()
        saveImage("recovery-${if (dark) "dark" else "light"}-top")
        val footer = "Volver a iniciar sesión"
        compose.onNodeWithText(footer).performScrollTo()
        assertLinkStyle(text = footer, action = footer, dark = dark)
        saveImage("recovery-${if (dark) "dark" else "light"}-footer")
    }

    private fun setContent(dark: Boolean, content: @Composable () -> Unit) {
        compose.setContent { PagoControlTheme(dark) { content() } }
    }

    private fun assertLinkStyle(text: String, action: String, dark: Boolean) {
        val annotated = compose.onNodeWithText(text).fetchSemanticsNode().config[SemanticsProperties.Text].single()
        val span = annotated.spanStyles.single { it.start == text.lastIndexOf(action) && it.end == text.length }
        assertEquals(FontWeight.Bold, span.item.fontWeight)
        assertEquals(if (dark) TextDecoration.None else TextDecoration.Underline, span.item.textDecoration)
        if (dark) assertEquals(EntryColors.Gold, span.item.color)
        val actionStart = text.lastIndexOf(action)
        if (actionStart > 0) {
            val separator = annotated.spanStyles.single { it.start == actionStart - 1 && it.end == actionStart }
            assertEquals(6.sp, separator.item.letterSpacing)
        }
    }

    private fun saveImage(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(context.getExternalFilesDir(null), "auth-previews").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { output ->
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, output)
        }
    }
}
