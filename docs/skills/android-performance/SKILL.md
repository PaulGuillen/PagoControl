---
name: android-performance
description: Medir y mejorar arranque, fluidez, memoria y consumo de recursos Android Compose. Usar ante lentitud observada o requisitos de rendimiento explícitos.
---

# Medir antes de optimizar

## Procedimiento

1. Define escenario, dispositivo y métrica: inicio, tiempo de interacción, frames, memoria o coste de red. Registra una línea base reproducible.
2. Usa una variante cercana a release y herramientas adecuadas; debug puede distorsionar resultados. Incorpora benchmarks o perfiles solo cuando aporten valor mantenible.
3. Localiza IO/CPU en main, listeners repetidos, recomposición costosa, imágenes grandes y retención de recursos.
4. Aplica cambios acotados: paginación, caché con límites, claves de listas, reducción de trabajo y cierre de recursos. No agregues anotaciones de estabilidad sin comprobar su contrato.
5. Mide exactamente el mismo recorrido tras el cambio y revisa comportamiento offline, accesibilidad y consumo.
6. Evalúa arranque de SDKs y carga DI sin retrasar funciones necesarias ni perder observabilidad de fallos importantes.

## Comprobar y entregar

Entrega evidencia antes/después, condiciones de medición y limitaciones. Si no hay dispositivo o medición, presenta hipótesis y cambios verificables sin afirmar mejoras cuantitativas inventadas.
