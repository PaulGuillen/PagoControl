package com.devpaul.features.templates.pdf

internal data class PdfPageSlice(
    val top: Int,
    val bottom: Int,
) {
    val height: Int get() = bottom - top
}

internal fun calculatePageSlices(
    contentHeight: Int,
    maximumPageHeight: Int,
    minimumFillRatio: Float = 0.72f,
    rowInkScore: (Int) -> Int,
): List<PdfPageSlice> {
    require(contentHeight > 0)
    require(maximumPageHeight > 0)
    require(minimumFillRatio in 0.5f..1f)

    val slices = mutableListOf<PdfPageSlice>()
    var top = 0
    while (top < contentHeight) {
        val nominalBottom = (top + maximumPageHeight).coerceAtMost(contentHeight)
        if (nominalBottom == contentHeight) {
            slices += PdfPageSlice(top, contentHeight)
            break
        }

        val minimumBottom = top + (maximumPageHeight * minimumFillRatio).toInt()
        var bestBottom = nominalBottom
        var bestScore = Int.MAX_VALUE
        for (candidate in nominalBottom downTo minimumBottom) {
            val score = rowInkScore(candidate)
            if (score < bestScore) {
                bestScore = score
                bestBottom = candidate
                if (score == 0) break
            }
        }

        val bottom = bestBottom.coerceIn(top + 1, nominalBottom)
        slices += PdfPageSlice(top, bottom)
        top = bottom
    }
    return slices
}
