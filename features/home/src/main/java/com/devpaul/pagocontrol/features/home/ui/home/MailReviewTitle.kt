package com.devpaul.pagocontrol.features.home.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.devpaul.pagocontrol.features.home.R

@Composable
internal fun MailReviewTitle(pulsing: Boolean) {
    if (pulsing) {
        val transition = rememberInfiniteTransition(label = "newMail")
        val pulse by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(950), RepeatMode.Reverse),
            label = "reviewPulse",
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.graphicsLayer {
                alpha = 0.65f + pulse * 0.35f
                scaleX = 1f + pulse * 0.025f
                scaleY = 1f + pulse * 0.025f
            },
        ) {
            Text(stringResource(R.string.notifications_review), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.gmail_review_new), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    } else {
        Text(stringResource(R.string.notifications_review), style = MaterialTheme.typography.titleMedium)
    }
}
