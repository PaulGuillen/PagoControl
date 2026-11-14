package com.devpaul.pagocontrol.designsystem

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PagoHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().heightIn(min = 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PagoBrand(title, Modifier.weight(1f))
    }
}
