---
name: android-remote-config
description: Implementar flags con Firebase Remote Config y estrategias de actualización Android. Usar para activación gradual, desactivación operativa y compatibilidad de versiones.
---

# Controlar funcionalidades remotamente

## Procedimiento

1. Define cada parámetro con tipo, default local, dueño y comportamiento sin red. Evita que una respuesta remota inválida deje la app inutilizable.
2. Expón configuración validada como estado; separa fetch, activación y consumo. Define cuándo un cambio puede aplicarse sin romper un flujo abierto.
3. Usa flags para presentación o disponibilidad, no como única autorización de compras, cuotas o acceso a datos. El backend valida derechos reales.
4. Si se requiere versión mínima, define comparison por código de versión, alcance por ambiente y recuperación ante configuración errónea.
5. Coordina actualización flexible/inmediata con disponibilidad real de la tienda. Evita bloquear builds de desarrollo o dispositivos donde no exista la actualización.
6. Registra cambios y plan de reversión de flags; distingue build del cliente de configuración remota publicada.

## Comprobar y entregar

Prueba defaults, ausencia de red, parámetros incorrectos, activación durante uso y actualización no disponible. Entrega tabla de parámetros y estrategia de rollback. Publicar valores remotos es un paso externo distinto de implementar su lectura.
