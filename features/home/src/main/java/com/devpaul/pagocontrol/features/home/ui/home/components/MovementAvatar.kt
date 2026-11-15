package com.devpaul.pagocontrol.features.home.ui.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.devpaul.pagocontrol.designsystem.InstitutionAvatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun MovementAvatar(source: String, packageName: String) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(null, context, source, packageName) {
        value = null
        val appPackage = when {
            packageName.isNotBlank() -> packageName
            source.equals("Yape", ignoreCase = true) -> "com.bcp.innovacxion.yapeapp"
            else -> null
        }
        value = appPackage?.let {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.packageManager.getApplicationIcon(it)
                        .toBitmap(width = 132, height = 132).asImageBitmap()
                }.getOrNull()
            }
        }
    }
    val bitmap = icon
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(44.dp))
    } else {
        InstitutionAvatar(source)
    }
}
