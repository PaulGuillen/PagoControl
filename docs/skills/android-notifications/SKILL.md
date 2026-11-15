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

## Avisos de movimientos de PagoControl

El toolbar compartido `PagoHeader` muestra la marca y el titulo sin campana ni accion de notificaciones. Los avisos son locales y se controlan desde Notificaciones en Perfil y desde el permiso/canal de Android; no requieren FCM. Solicita `POST_NOTIFICATIONS` en Android 13 o superior al activar la opcion. Si Android o el canal bloquea los avisos, muestra el estado real y permite abrir sus ajustes; conserva la captura de movimientos como configuracion independiente.

Emite avisos de ingreso o egreso solo despues de guardar un movimiento automatico nuevo o de confirmar uno pendiente. La operacion de Room devuelve el registro efectivamente insertado/confirmado para evitar avisos de capturas repetidas, confirmaciones repetidas, pendientes, descartes, historiales y fuentes desactivadas. Un fallo al mostrar el aviso no revierte el movimiento. Verifica la sesion y preferencia actual antes de publicarlo, limpia avisos al cambiar de cuenta/cerrar sesion o desactivar la preferencia y usa contenido privado en la pantalla bloqueada. El toque abre la aplicacion mediante un PendingIntent explicito e inmutable y conserva el flujo normal de autenticacion.

## Recuperacion desde Firestore

La descarga inicial, las paginas incrementales y las confirmaciones remotas actualizan Room sin invocar `MovementNotifier`. Conserva los avisos solo en las acciones locales de captura automatica o confirmacion nuevas; volver a abrir Home, recuperar una cuenta o reconocer una subida pendiente no debe volver a notificar movimientos historicos.
