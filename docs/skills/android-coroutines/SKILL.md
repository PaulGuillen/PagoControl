---
name: android-coroutines
description: Diseñar concurrencia estructurada, cancelación y flujos reactivos en Kotlin Android. Usar para búsquedas, observación, autosave y operaciones simultáneas.
---

# Controlar trabajo asíncrono

## Procedimiento

1. Define propietario y duración de cada trabajo: pantalla, grafo, sesión o trabajo durable. Usa scopes acordes; evita `GlobalScope`.
2. Mantén APIs main-safe. Mueve IO bloqueante a un dispatcher apropiado y trabajo CPU intenso a Default; permite sustituir dispatchers en pruebas.
3. Propaga cancelación incluso dentro de `runCatching` o capturas generales. Libera listeners y recursos con `finally` o `awaitClose`.
4. Para búsqueda, decide debounce y sustitución con operadores como `mapLatest`; para escrituras, decide serialización, mutex o revisión por versión. Cancelar una llamada no revierte una escritura que llegó al servidor.
5. Define estado inicial y política `stateIn`/`shareIn`, número de suscriptores y coste de reinicio. Evita collectors duplicados por recomposición.
6. Si una operación debe sobrevivir al proceso, diseña persistencia y un mecanismo durable apropiado, como WorkManager si el producto lo necesita. Una coroutine en Application no ofrece esa garantía.

## Comprobar y entregar

Prueba orden, cancelación, resultados obsoletos y dos acciones simultáneas con tiempo virtual. Documenta qué continúa al salir de la pantalla y qué se cancela.
