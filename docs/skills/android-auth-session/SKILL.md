---
name: android-auth-session
description: Implementar autenticación Firebase y contratos de sesión independientes del proveedor. Usar para login, registro, recuperación, perfil y cierre de sesión.
---

# Gestionar identidad y sesión

## Procedimiento

1. Separa identidad autenticada, perfil y permisos. Define contratos mínimos de sesión en Kotlin puro y adaptadores del proveedor en datos.
2. Modela inicio, registro y recuperación mediante casos de uso; valida entradas sin duplicar reglas del servidor como si fueran autorización.
3. Observa cambios reales de sesión y expiración. No uses un booleano persistido como prueba de identidad.
4. Define recuperación ante creación de cuenta exitosa y escritura de perfil fallida. Haz idempotente la inicialización del perfil.
5. Implementa logout como coordinación: detener listeners/trabajos de cuenta, resolver borradores según política, limpiar estado y retirar rutas protegidas.
6. Un limitador local mejora UX, pero un límite efectivo requiere servidor. Evita mensajes que revelen cuentas existentes cuando el flujo/proveedor lo permita.

## Comprobar y entregar

Prueba cuenta nueva, contraseña errónea, sesión restaurada/expirada, perfil ausente y dos cuentas consecutivas. Verifica que no se muestren datos del usuario anterior ni queden callbacks activos de esa sesión.
