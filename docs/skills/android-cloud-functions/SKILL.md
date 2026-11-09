---
name: android-cloud-functions
description: Implementar backend Firebase Cloud Functions para operaciones privilegiadas y transaccionales. Usar para callables, cuotas y acciones que no pueden confiar en Android.
---

# Implementar operaciones de servidor

## Procedimiento

1. Define contrato de entrada/salida, región y errores compatibles con el cliente. Verifica runtime y SDK soportados antes de fijar dependencias.
2. Extrae lógica verificable de negocio de la integración con Functions/Admin SDK. Valida payload, identidad y autorización en cada operación.
3. Exige App Check donde corresponda al modelo de abuso; no lo uses como sustituto de permisos por usuario.
4. Para cuotas, reserva en transacción con reloj del servidor, lease y clave idempotente. No invoques servicios externos dentro del callback transaccional que pueda reejecutarse.
5. Define recuperación, timeout y consecuencias si el cliente pierde conexión después del éxito. Usa secretos del servidor y logs sin contenido sensible.
6. Versiona configuración y contratos. Una operación lenta o externa necesita límites y estrategia de reintento explícitos.

## Comprobar y entregar

Prueba autorización, duplicados, concurrencia y fallo del proveedor con dobles controlados; usa emulador para integración. Entrega código, configuración y pasos de despliegue sin ejecutar publicación como parte implícita de escribir la función.
