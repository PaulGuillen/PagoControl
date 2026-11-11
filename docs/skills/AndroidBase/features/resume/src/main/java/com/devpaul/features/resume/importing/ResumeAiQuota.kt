package com.devpaul.features.resume.importing

import android.content.Context
import com.google.firebase.functions.FirebaseFunctions
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

internal enum class ResumeAiAvailability { Checking, Available, Limited, Busy, Unavailable }

internal data class ResumeAiQuotaStatus(
    val availability: ResumeAiAvailability = ResumeAiAvailability.Checking,
    val nextAvailableAtMillis: Long? = null,
) {
    fun canUseAi(policy: ResumeImportPolicy): Boolean =
        policy.aiEnhancementEnabled && availability == ResumeAiAvailability.Available
}

internal fun parseResumeAiQuota(response: Any?): ResumeAiQuotaStatus {
    val data = response as? Map<*, *> ?: return ResumeAiQuotaStatus(ResumeAiAvailability.Unavailable)
    val nextAvailableAt = (data["nextAvailableAtMs"] as? Number)?.toLong()?.takeIf { it > 0 }
    val availability = when (data["status"]) {
        "available" -> ResumeAiAvailability.Available
        "limited" -> if (nextAvailableAt != null) ResumeAiAvailability.Limited else ResumeAiAvailability.Unavailable
        "busy" -> ResumeAiAvailability.Busy
        else -> ResumeAiAvailability.Unavailable
    }
    return ResumeAiQuotaStatus(availability, nextAvailableAt)
}

internal object ResumeAiQuota {
    private val mutableStatus = MutableStateFlow(ResumeAiQuotaStatus())
    val status = mutableStatus.asStateFlow()

    fun markChecking() {
        mutableStatus.value = ResumeAiQuotaStatus()
    }

    suspend fun refresh(context: Context) {
        markChecking()
        mutableStatus.value = try {
            val result = FirebaseFunctions.getInstance("us-central1")
                .getHttpsCallable("getResumeAiQuota")
                .apply { setTimeout(12, TimeUnit.SECONDS) }
                .call(mapOf("deviceHash" to resumeAiDeviceHash(context)))
                .await()
            parseResumeAiQuota(result.data)
        } catch (cancelled: CancellationException) {
            mutableStatus.value = ResumeAiQuotaStatus(ResumeAiAvailability.Unavailable)
            throw cancelled
        } catch (_: Exception) {
            ResumeAiQuotaStatus(ResumeAiAvailability.Unavailable)
        }
    }
}
