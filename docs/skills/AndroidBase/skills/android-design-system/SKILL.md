---
name: android-design-system
description: Definir tema Material 3, tokens visuales y componentes Compose compartidos. Usar para crear o unificar el lenguaje visual de una app Android.
---

# Crear el sistema de diseño

## Procedimiento

1. Identifica identidad, jerarquía visual y pantallas representativas. Define colores semánticos, tipografía, formas, espaciado y tamaños de interacción.
2. Coloca tema y componentes compartidos en `core-designsystem`. Deja la persistencia de preferencias en infraestructura y las pantallas de negocio en su feature.
3. Define paletas clara/oscura y política de color dinámico. Evita colores incrustados en pantallas que impidan adaptar contraste o marca.
4. Implementa componentes con valores y callbacks, `Modifier`, estados enabled/loading/error y semántica accesible. No dependas de repositorios, sesión ni NavController.
5. Extrae componentes usados por varias features cuando compartan semántica real. Conserva local un componente específico de un solo flujo.
6. Añade previews representativas con textos largos, tamaños de fuente grandes y ambos temas. Documenta variantes y parámetros útiles.

## Comprobar y entregar

Entrega tokens y catálogo funcional de componentes. Revisa coherencia de foco, error, deshabilitado y carga. Verifica que una pantalla de ejemplo pueda construirse sin introducir lógica de negocio en el sistema visual.
