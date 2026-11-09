---
name: android-backend-testing
description: Probar Functions y reglas Firestore con node:test, dobles transaccionales y Firebase Emulator Suite. Usar para validar autorización, esquemas y concurrencia del backend.
---

# Verificar el backend

## Procedimiento

1. Separa pruebas de lógica pura de pruebas de SDK/reglas. Node con almacén en memoria comprueba lógica, pero no permisos efectivos ni semántica completa de Firestore.
2. Prueba reservas, límites temporales, idempotencia, vencimiento y recuperación de funciones con reloj controlado.
3. Carga reglas completas en emulador con un proyecto demo aislado. Prueba anónimo, propietario y otro UID; crea datos de preparación mediante un contexto de administración acotado.
4. Incluye payload con campos extra, tipos inválidos, modificaciones prohibidas, queries y escrituras que deban ser atómicas.
5. Verifica configuración del callable e integración donde sea viable. La lógica extraída no demuestra por sí sola enforcement real de Auth/App Check.
6. Usa lockfile para `npm ci` cuando exista; si falta, genera dependencias reproducibles antes de exigir ese comando en CI.

## Comprobar y entregar

Ejecuta suites declaradas en `package.json`, identifica runtime/emuladores requeridos y reporta qué frontera cubre cada suite. No ejecutes pruebas destructivas contra producción ni asumas que pasar local equivale a despliegue verificado.
