---
name: android-datastore
description: Persistir preferencias y datos locales pequeños con DataStore y migraciones Android. Usar para idioma, apariencia, onboarding y ajustes de usuario.
---

# Persistir preferencias

## Procedimiento

1. Decide si el dato pertenece a preferencias globales, cuenta o borrador. DataStore es adecuado para estado pequeño; evalúa almacenamiento estructurado para colecciones grandes o consultas.
2. Crea una instancia por archivo y proceso bajo un dueño claro. Expón contratos de lectura reactiva/escritura sin filtrar claves a la UI.
3. Define defaults, valores desconocidos y migraciones desde versiones previas. No reemplaces silenciosamente datos de usuario ante un error de lectura.
4. Usa actualizaciones transaccionales y evita ciclos donde la lectura dispara escritura continuamente.
5. Separa claves por usuario cuando sea necesario y define limpieza en logout. No guardes contraseñas ni asumas cifrado por usar DataStore.
6. Para migrar datos a nube, conserva el origen hasta confirmar éxito e idempotencia; la primera lectura no debe borrar la única copia.

## Comprobar y entregar

Prueba defaults, escritura, reinicio, valores antiguos, corrupción y cambio de cuenta. Entrega mapa de claves/esquema, política de migración y comportamiento de recuperación.
