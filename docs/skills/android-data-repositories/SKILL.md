---
name: android-data-repositories
description: Implementar repositorios y fuentes de datos con mapeo entre DTO y dominio. Usar al conectar contratos de negocio con persistencia o servicios externos.
---

# Implementar la capa de datos

## Procedimiento

1. Parte del contrato del dominio y documenta consistencia, orden, paginación y disponibilidad de cada operación.
2. Define fuentes remotas/locales con responsabilidades pequeñas. El repositorio coordina el origen y mapea modelos; la UI no conoce SDK ni DTO.
3. Valida esquemas y datos externos antes de exponerlos. Diferencia documento inexistente, respuesta inválida, permiso denegado, desconexión y fallo temporal.
4. Traduce errores técnicos a fallos del contrato y propaga cancelación. No filtres mensajes del proveedor al usuario.
5. Define idempotencia, límites de reintentos y propietario de los listeners. Libera suscripciones cuando se cancela la observación o cambia la sesión.
6. Inyecta fuentes, reloj y dispatchers necesarios para pruebas. Mantén serialización/versionado del almacenamiento fuera de las entidades si acopla el dominio al proveedor.

## Comprobar y entregar

Verifica mapeos, datos corruptos, actualización parcial, cancelación y cambio de cuenta. Entrega implementación, registro DI y pruebas de comportamiento del contrato; no pruebes únicamente que una llamada fue delegada.
