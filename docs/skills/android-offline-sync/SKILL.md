---
name: android-offline-sync
description: Diseñar almacenamiento sin conexión, autosave y sincronización con resolución de conflictos. Usar cuando una app deba conservar cambios y reconciliarlos con el servidor.
---

# Diseñar sincronización

## Procedimiento

1. Define fuente de verdad y garantías visibles: guardado local, pendiente de sincronizar y confirmado por servidor son estados diferentes.
2. Identifica capacidades offline del proveedor existente antes de agregar otra caché. Si se necesita cola propia, define esquema durable, reintentos e idempotencia.
3. Usa revisiones/versiones para evitar que respuestas antiguas sobrescriban cambios recientes. Decide política de conflicto y borrado con tombstones si corresponde.
4. En autosave, limita frecuencia y serializa escrituras. Guarda periódicamente datos importantes; `ON_STOP` o un flush final no garantizan ejecución frente a muerte del proceso.
5. Aísla datos y trabajos por cuenta. Antes de ejecutar una tarea pendiente, confirma que sigue autorizada para su propietario.
6. Evalúa WorkManager o base local adicional solo cuando se requiera trabajo durable o consultas que el almacenamiento actual no resuelva.

## Comprobar y entregar

Prueba pérdida de red, muerte/reinicio, edición concurrente, borrado y cambio de sesión durante una escritura. Entrega estados de sincronización y reglas de resolución comprensibles para la UI.
