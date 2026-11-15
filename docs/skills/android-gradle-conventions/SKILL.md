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

## Dependencias centralizadas con DependenceUtil

Declara las librerias externas y sus configuraciones de consumo en `buildSrc/src/main/kotlin/dependence/DependenceUtil.kt`, mediante funciones de extension `Project` como `composeImplementation()`, `firestoreImplementation()` o `roomImplementation()`. Resuelve alias desde el catalogo `libs.versions.toml`; no dupliques coordenadas ni versiones. Los build.gradle.kts de producto llaman funciones o aplican convenciones, sin bloques implementation(libs...) repetidos.

Los plugins `plugin.feature`, `plugin.application`, `plugin.compose`, `plugin.koin` y `plugin.kotlin.lib` llaman las funciones comunes a sus consumidores. Las features reciben Compose, navegacion, Koin y pruebas desde sus convenciones. Firebase, Room o DataStore se agregan con funciones solo a quienes los usan. Conserva las dependencias project(...) visibles en los modulos o su convencion compartida y la configuracion particular (namespace, buildTypes, exportacion de esquemas) en el propietario. Los artefactos Gradle necesarios para compilar buildSrc permanecen en buildSrc/build.gradle.kts: ese build no puede usar funciones que todavia esta compilando. AndroidBase es referencia y no se modifica.

Al refactorizar, conserva los scopes implementation, ksp, debugImplementation y androidTestImplementation, los BOM y el grafo entre modulos. Evita que dos convenciones declaren el mismo grupo en un consumidor. Valida compilacion debug, compilacion de pruebas instrumentadas, unit tests y una variante release para comprobar los scopes.
