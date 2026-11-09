---
name: android-firestore
description: Diseñar colecciones Firestore, consultas, reglas de seguridad e índices para Android. Usar al persistir documentos remotos y proteger acceso por usuario.
---

# Diseñar datos y reglas Firestore

## Procedimiento

1. Define documentos, propietario, campos requeridos, tipos, versión del esquema y patrones de consulta. Calcula implicaciones de lecturas, tamaño y paginación.
2. Diseña reglas junto al modelo: autenticación, UID propietario, campos permitidos/inmutables y validación de transiciones.
3. Usa tiempo del servidor para restricciones globales. Decide transacción frente a batch según si debes leer y validar estado concurrente.
4. Recuerda que permisos de reglas coincidentes se combinan; una regla amplia permisiva puede invalidar la protección de una ruta concreta. Las reglas no filtran resultados: las consultas deben satisfacer sus restricciones.
5. Mantén índices y reglas completas versionados. Integra fragmentos del proyecto original en el archivo completo correspondiente, nunca los publiques como reemplazo directo.
6. El Admin SDK omite reglas; valida autorización y payload también en Functions. Define limpieza y migración de subcolecciones explícitamente.

## Comprobar y entregar

Prueba usuario anónimo, propietario, otro UID, datos inválidos y escrituras concurrentes con emulador. Entrega esquema, consultas, índices y reglas completas del nuevo proyecto.
