package com.devpaul.pagocontrol.features.home.ui.home.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.devpaul.pagocontrol.designsystem.PagoControlTheme
import com.devpaul.pagocontrol.features.home.domain.entity.Dashboard
import com.devpaul.pagocontrol.features.home.ui.home.HomeScreen
import com.devpaul.pagocontrol.features.home.ui.home.HomeUiState
import com.devpaul.pagocontrol.features.home.ui.home.model.HomeContent

@Preview(name = "Home claro", showBackground = true)
@Composable
private fun HomeLightPreview() {
    PagoControlTheme(false) {
        HomeScreen(
            state = previewState(),
            name = "Carlos",
            onIntent = {},
            themeIndex = 1,
            onThemeSelected = {})
    }
}

@Preview(name = "Home oscuro", showBackground = true)
@Composable
private fun HomeDarkPreview() {
    PagoControlTheme(true) {
        HomeScreen(
            state = previewState(),
            name = "Carlos",
            onIntent = {},
            themeIndex = 2,
            onThemeSelected = {})
    }
}

private fun previewState() = HomeUiState(
    content = HomeContent.Ready(
        Dashboard(
            balanceCents = 458050,
            incomeCents = 685000,
            expenseCents = 226950,
            incomeCount = 18,
            expenseCount = 34,
            weeklyFlow = listOf(21, 28, 17, 35, 24, 42, 14),
            monthlyFlow = listOf(14, 18, 16, 20),
            movements = emptyList()
        ),
    )
)
