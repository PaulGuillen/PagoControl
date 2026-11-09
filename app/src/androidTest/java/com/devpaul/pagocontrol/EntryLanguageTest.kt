package com.devpaul.pagocontrol

import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.devpaul.pagocontrol.features.auth.R as AuthResources
import com.devpaul.pagocontrol.features.onboarding.R as OnboardingResources
import com.devpaul.pagocontrol.features.splash.R as SplashResources
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EntryLanguageTest {
    @Test
    fun entryScreensRemainSpanishWithEnglishSystemResources() {
        val application = ApplicationProvider.getApplicationContext<Context>()
        val configuration = Configuration(application.resources.configuration).apply {
            setLocale(Locale.ENGLISH)
        }
        val context = application.createConfigurationContext(configuration)

        assertEquals("SINCRONIZACIÓN INTELIGENTE", context.getString(SplashResources.string.splash_badge))
        assertEquals("Omitir", context.getString(OnboardingResources.string.onboarding_skip))
        assertEquals("Bienvenido de nuevo", context.getString(AuthResources.string.flow_login_title))
        assertEquals("Crea tu cuenta", context.getString(AuthResources.string.flow_register_title))
        assertEquals("¿Olvidaste tu contraseña?", context.getString(AuthResources.string.flow_forgot_title))
    }
}
