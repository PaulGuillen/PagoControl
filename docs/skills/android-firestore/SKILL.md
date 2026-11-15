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

## Movimientos de PagoControl

Ubica los contratos en `features/home/domain/` y las fuentes/codec en `features/home/data/`, siguiendo la organizacion por capas de Auth. Core queda reservado a capacidades globales compartidas; AndroidBase es una referencia y no se modifica. `RoomPaymentRepository` alimenta Home exclusivamente desde Room. `FirestorePaymentSyncRepository` coordina una sola sincronizacion por cuenta; `FirebasePaymentCloudStore` contiene el SDK y `FirestorePaymentCodec` valida los documentos.

Los documentos viven en `users/{uid}/payments/{sha256(id)}` y el control en `users/{uid}/paymentSync/state`. Una transaccion sube hasta 20 registros y avanza una revision monotona por cuenta. Lee todos los documentos antes de escribir; usa writeId para reintentos idempotentes y conserva descartes como tombstones. Las paginas remotas filtran revision posterior al checkpoint, con limite 100; Room guarda pagina y cursor en la misma transaccion. Los cambios propios confirmados no se descargan otra vez si no hay revisiones ajenas intermedias.

Las reglas conservan el acceso existente a perfiles, aislan las subcolecciones por UID y validan esquema, importes, revisiones y transiciones. Las exenciones de indices de textos y writeId viven en `firebase/firestore.indexes.json`; la consulta incremental usa el indice simple de revision. Comprueba las reglas con `npm test` en `firebase/rules-tests`; usa el proyecto demo y los puertos 8180/9199 de `firebase/sync-tests.json`, nunca produccion para pruebas. Para la integracion Android de `features/home`, configura ADB reverse de esos puertos y ejecuta `:features:home:connectedDebugAndroidTest` con la clase `com.devpaul.pagocontrol.features.home.data.sync.FirestorePaymentSyncIntegrationTest` con ambos emuladores activos.
