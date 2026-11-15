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

## Room y Firestore en PagoControl

La UI siempre lee Room. La primera recuperacion por UID es paginada y reanudable; los siguientes accesos consultan solo revisiones nuevas, sin volver a cargar el historial. `pendingWriteId` es la cola durable y `remoteRevision` la base de conflicto. Captura, confirmacion y descarte guardan el cambio local y su token en la misma operacion. Una respuesta de subida solo limpia el token que envio; si el usuario cambio el movimiento durante la peticion, conserva el token nuevo y actualiza su revision base.

Aisla movimientos, fuentes y checkpoints por ownerId. La migracion Room 1 a 2 conserva los datos anteriores sin borrado destructivo y los asigna una sola vez a la primera cuenta autenticada; nunca los vuelvas a asignar al cambiar de usuario. Las fuentes monitorizadas siguen siendo una preferencia local del dispositivo por cuenta. La hidratacion remota no dispara notificaciones locales.

Agrupa rafagas, serializa la sincronizacion, limita las comprobaciones remotas a una por minuto y usa lotes de 20 escrituras/paginas de 100 lecturas. No hay polling ni listeners por pantalla: sesion, retorno al primer plano, conexion recuperada, cambios locales y accion Reintentar despiertan el coordinador. El limite de un minuto es entre comprobaciones, no una consulta periodica. Ante fallo, conserva Room y reintenta con espera hasta tres intentos; errores de permiso o validacion esperan otro disparador. Si el proceso termina, los pendientes se reanudan al volver a iniciarlo; no prometas ejecucion con el proceso muerto sin integrar trabajo durable del sistema.

Los descartes son terminales; una escritura obsoleta no restaura un movimiento descartado. Una confirmacion local puede promover un pendiente remoto aunque se haya perdido la respuesta de su subida inicial. Comprueba paginacion interrumpida, respuesta perdida tras commit, cambios durante subida, cuentas distintas y reapertura de Room.

En Home, muestra el boton Reintentar solo cuando `PaymentSyncState.hasError` sea verdadero y deshabilitalo mientras haya una sincronizacion en curso. Al recuperarse, el boton desaparece; el funcionamiento normal es automatico.
