---
name: android-notifications
description: Implementar notificaciones Android, permisos, canales y apertura segura de destinos. Usar para avisos locales o integración de mensajería cuando el producto lo requiera.
---

# Diseñar avisos y entradas

## Procedimiento

1. Distingue aviso local de push; agrega FCM solo si existe entrega remota requerida. Define propósito, prioridad y política de duplicados.
2. Crea canales coherentes y solicita permisos según la versión Android y contexto del usuario. La app debe seguir funcionando si se deniegan.
3. Configura PendingIntent con identidad, flags y mutabilidad mínimos adecuados. Valida IDs recibidos y no incluyas datos sensibles innecesarios.
4. Centraliza el manejo de apertura con app cerrada, abierta o en segundo plano. Espera autenticación antes de mostrar contenido privado.
5. Si guardas historial, define retención, aislamiento por cuenta y borrado. Notificación mostrada, leída y abierta son estados distintos.
6. Para push, gestiona rotación de token y asociación/desasociación con cuenta en backend; no consideres un token FCM prueba de identidad.

## Comprobar y entregar

Prueba permiso denegado, canal desactivado, doble toque, entrada sin sesión y usuario distinto. Comprueba que la navegación se consume una vez y que logout no deja avisos privados de otra cuenta.
