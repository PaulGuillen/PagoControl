---
name: android-documents
description: Implementar importación y exportación de documentos Android con PDFBox, ML Kit y APIs de archivos. Usar para PDF, OCR, escáner y compartir archivos.
---

# Procesar documentos

## Procedimiento

1. Define formatos admitidos, tamaño máximo y salida esperada. Usa selectores y permisos URI adecuados; no asumas que una URI tiene una ruta de archivo accesible.
2. Valida contenido además del MIME/nombre. Trata PDF corrupto, protegido, enorme o con páginas sin texto como casos explícitos.
3. Separa extracción de texto, OCR, parsing del dominio y asistencia IA opcional. Conserva el resultado útil si una etapa adicional falla.
4. Ejecuta procesamiento fuera del hilo principal, limita memoria y cierra streams, bitmaps y documentos. Evita cargar todas las páginas de un PDF grande simultáneamente.
5. Para exportar, define paginación, fuentes, tamaños y metadatos. Comprueba que el renderizado visual y la estructura de datos correspondan.
6. Comparte mediante URI y permisos temporales; no expongas rutas internas. Define limpieza de archivos temporales y recuperación tras cancelación.

## Comprobar y entregar

Prueba documentos de muestra sin datos reales: texto, escaneo, múltiples páginas, corrupto y vacío. Abre el PDF exportado en otro lector y verifica cortes, caracteres y permisos.
