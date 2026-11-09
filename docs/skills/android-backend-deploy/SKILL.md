---
name: android-backend-deploy
description: Preparar y ejecutar despliegues Firebase de Functions, reglas e índices por ambiente. Usar al entregar backend con secretos y comprobaciones de compatibilidad Android.
---

# Desplegar backend por ambiente

## Procedimiento

1. Lee configuración completa, identifica project ID, región, codebase y recursos afectados. Nunca infieras producción por el proyecto activo de una CLI.
2. Define separación dev/prod, permisos de despliegue, secretos, parámetros y facturación requerida. No vuelques valores secretos en logs ni archivos versionados.
3. Instala dependencias reproducibles y ejecuta pruebas. Compila/transpila solo si el backend lo necesita; un proyecto JavaScript puede no tener tarea build.
4. Integra reglas e índices completos antes de publicarlos. El `firebase.json` del origen solo despliega Functions; sus fragmentos de reglas no bastan para publicar Firestore.
5. Define comandos con `--project` y selectores de recursos explícitos. Despliega primero cambios compatibles con clientes existentes y prueba en dev.
6. Si la tarea autoriza desplegar, ejecuta el alcance previsto y verifica endpoints, Auth/App Check y lectura/escritura controladas. Si pide preparación, entrega comandos y configuración listos sin declarar despliegue.

## Comprobar y entregar

Registra revisión, proyecto, región, recursos cambiados y smoke tests reales. Conserva configuración anterior y plan de recuperación; considera migraciones de datos antes de revertir código. No borres cuotas ni datos reales para simular éxito.
