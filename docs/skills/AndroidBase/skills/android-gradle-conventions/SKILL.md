---
name: android-gradle-conventions
description: Configurar catálogo de versiones, plugins de convención, toolchains y variantes Gradle en Android multimódulo. Usar al centralizar o ampliar el sistema de build.
---

# Configurar Gradle y variantes

## Procedimiento

1. Inspecciona wrapper, catálogo y plugins existentes. Separa plugins para aplicación, librería Android, Kotlin puro y feature Compose.
2. Usa `buildSrc` si mantiene el tamaño y estilo del proyecto; evalúa un included build `build-logic` cuando el aislamiento y mantenimiento lo justifiquen. Registra la decisión.
3. Centraliza SDK/toolchains y configuración repetida. Añade librerías específicas en su consumidor: PDF, Billing y red no son dependencias universales de una feature.
4. Modela ambiente y acceso como dimensiones distintas solo si existen productos que lo requieran. Define IDs, endpoints y configuración Firebase por ambiente. Deshabilita combinaciones que no deban distribuirse.
5. Comprueba compatibilidad de KSP y procesadores. Revisa Kotlin integrado de la versión de AGP antes de aplicar plugins Kotlin adicionales.
6. Optimiza cachés y paralelismo tras medir compatibilidad; no desactives lint globalmente para ocultar fallos.

## Comprobar y entregar

Lista tareas reales, compila una librería Kotlin y una feature Android, y genera una variante debug. Entrega matriz de variantes, catálogo sin versiones duplicadas injustificadas y explicación de propiedades locales/CI.
