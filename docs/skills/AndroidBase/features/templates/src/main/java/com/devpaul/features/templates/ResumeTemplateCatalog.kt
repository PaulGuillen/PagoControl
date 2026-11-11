package com.devpaul.features.templates

data class ResumeTemplateDefinition(
    val id: String,
    val displayName: String,
    val description: String,
    val hasSidebar: Boolean,
    val renderer: ResumeTemplateRenderer,
    val legacyIdentifiers: Set<String> = emptySet(),
)

enum class ResumeTemplateRenderer {
    Budapest,
    Riga,
    PragueEditorial,
    TokyoCompact,
}

object ResumeTemplateCatalog {
    const val BUDAPEST_ID = "budapest"
    const val RIGA_ID = "riga"
    const val PRAGUE_EDITORIAL_ID = "prague_editorial"
    const val TOKYO_COMPACT_ID = "tokyo_compact"

    val templates: List<ResumeTemplateDefinition> = listOf(
        ResumeTemplateDefinition(
            id = BUDAPEST_ID,
            displayName = "Budapest",
            description = "Diseño profesional de dos columnas con títulos en mayúsculas.",
            hasSidebar = true,
            renderer = ResumeTemplateRenderer.Budapest,
            legacyIdentifiers = setOf("classic_sidebar", "Clásico Sidebar", "Clásico con barra lateral"),
        ),
        ResumeTemplateDefinition(
            id = RIGA_ID,
            displayName = "Riga",
            description = "Estilo editorial en blanco y negro con columnas y divisores finos.",
            hasSidebar = false,
            renderer = ResumeTemplateRenderer.Riga,
            legacyIdentifiers = setOf("editorial_line", "Editorial Line", "Línea editorial"),
        ),
        ResumeTemplateDefinition(
            id = PRAGUE_EDITORIAL_ID,
            displayName = "Praga",
            description = "Diseño sobrio con encabezado dividido y secciones estructuradas.",
            hasSidebar = false,
            renderer = ResumeTemplateRenderer.PragueEditorial,
            legacyIdentifiers = setOf("Prague"),
        ),
        ResumeTemplateDefinition(
            id = TOKYO_COMPACT_ID,
            displayName = "Tokio",
            description = "Formato compacto de una columna con secciones resaltadas en azul claro.",
            hasSidebar = false,
            renderer = ResumeTemplateRenderer.TokyoCompact,
            legacyIdentifiers = setOf("Tokyo"),
        ),
    )

    val default: ResumeTemplateDefinition = templates.first()

    fun find(idOrLegacyName: String): ResumeTemplateDefinition = templates.firstOrNull { template ->
        template.id == idOrLegacyName ||
            template.displayName.equals(idOrLegacyName, ignoreCase = true) ||
            template.legacyIdentifiers.any { it.equals(idOrLegacyName, ignoreCase = true) }
    } ?: default
}
