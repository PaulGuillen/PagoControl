package com.devpaul.features.resume.importing

import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

internal fun resumeAiDeviceHash(context: Context): String {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    return resumeAiDeviceHash(checkNotNull(androidId) { "Device identifier unavailable" })
}

internal fun resumeAiDeviceHash(androidId: String): String {
    require(androidId.isNotBlank()) { "Device identifier unavailable" }
    return MessageDigest.getInstance("SHA-256")
        .digest("cvfree:$androidId".toByteArray(Charsets.UTF_8))
        .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
}

internal class ResumeAiQuotaException(val nextAvailableAtMillis: Long?) : Exception()
