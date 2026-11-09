---
name: android-bootstrap
description: Crear la configuración inicial ejecutable de una app Android Kotlin y Compose con Gradle Kotlin DSL. Usar para preparar el entorno y obtener el primer build.
---

# Preparar la base Android

## Procedimiento

1. Revisa instrucciones del repositorio y herramientas instaladas. Define nombre, namespace, applicationId y plataformas mínimas según el producto.
2. Verifica en fuentes oficiales compatibilidad entre Android Studio, AGP, Gradle, JDK, Kotlin, Compose y KSP. Fija una combinación; no uses versiones dinámicas ni presupongas que la fotografía de CvFree sigue vigente.
3. Crea wrapper, `settings.gradle.kts`, catálogo y build raíz. Centraliza repositorios y configura toolchain/SDK de forma reproducible.
4. Crea `app`, Application y Activity con tema Compose y una pantalla mínima. Agrega módulos compartidos según las necesidades aprobadas.
5. Excluye propiedades locales, claves y keystores del control de versiones. Documenta variables requeridas mediante nombres y ejemplos sin valores reales.

## Comprobar y entregar

Ejecuta sincronización o build por wrapper y registra la variante compilada. Comprueba arranque en dispositivo/emulador cuando esté disponible. Entrega instrucciones reproducibles de instalación y ejecución; identifica bloqueos externos por separado. No incluyas Firebase ni backend vacío solo para poder compilar una pantalla inicial.
