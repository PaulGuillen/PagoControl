# CV por cuenta en Cloud Firestore

Los CV editables se guardan en `users/{uid}/resumes/{resumeId}`. No se suben fotos,
PDF ni otros archivos y no se utiliza Cloud Storage. El PDF se genera de nuevo al
exportar y se guarda en el destino elegido con el selector de archivos de Android.

Cada documento tiene `schemaVersion` (`"1"`), `content` (el JSON versionado de
`ResumeDocument`, incluyendo plantilla y formato), `updatedAt` (milisegundos del
cliente para ordenar) y `syncedAt` (timestamp del servidor). El contenido está
limitado a 750 000 bytes en el cliente para dejar margen al límite del documento.
No se debe usar `updatedAt` para decisiones de seguridad.

## Publicar las reglas antes de usar la nueva versión

Integra `resumes.rules` dentro de
`service cloud.firestore { match /databases/{database}/documents { ... } }`
en las reglas existentes de cada proyecto dev/prod y publícalas en Firebase Console.
Es un fragmento, no un reemplazo de las reglas completas. Conserva las reglas de
perfiles, calificaciones, opiniones Premium y cupos de IA. El despliegue de Cloud
Functions no publica estas reglas. No se han publicado reglas desde este cambio.

Elimina cualquier permiso general recursivo que permita leer o escribir CV de
otros usuarios: las reglas coincidentes se combinan con OR. Cada UID solo puede
listar, leer, escribir y eliminar su propia subcolección. No hace falta un índice
compuesto: la lista se ordena por `updatedAt` en el cliente.

## Migración de instalaciones anteriores

DataStore ya no recibe CV nuevos; solo se abre para recuperar el formato antiguo.
En **Mis CV**, pulsa **Migrar CV de este dispositivo** y confirma la cuenta de
destino. No se asignan automáticamente CV locales sin propietario a quien inicie
sesión. Una vez confirmada, la migración queda vinculada a ese UID en DataStore;
otra cuenta no puede importar esos datos.

Cada CV usa un identificador determinista `legacy_` más el SHA-256 de su ID antiguo.
Una transacción crea el documento solo si no existe: reintentar no duplica CV ni
sobrescribe ediciones posteriores. La copia antigua se elimina individualmente
solo después de que el servidor confirme la transacción. Una desconexión, fallo
de permisos o cierre de la pantalla conserva los CV aún no confirmados. Si falla
el borrado local después de la subida, el siguiente intento es idempotente.
La migración necesita conexión. Los CV antiguos no se mezclan con la lista de la
nube antes de migrar, y el botón sigue disponible si la subida no termina.

## Sin conexión y cambios de cuenta

Se utiliza la caché persistente del SDK Android de Firestore. Guardar y eliminar
encolan escrituras sin bloquear el editor cuando falta Internet; **Mis CV** indica
si hay cambios pendientes, datos de caché o un error. Un guardado local pendiente
no equivale a una copia confirmada en la nube. No desinstales ni borres los datos
de la app hasta que termine la sincronización. Un rechazo del servidor se muestra
como error; comprueba permisos/conexión y vuelve a guardar la edición si hace falta.

La lista se observa por UID, se vacía al cambiar de cuenta y se retira el listener
anterior. Los borradores y operaciones de autoguardado pendientes se vinculan al
UID para no guardarse en una cuenta distinta después de cerrar sesión.
La resolución de ediciones simultáneas entre dispositivos es la del SDK:
prevalece la última escritura del documento. No hay fusión por campo.

DataStore se mantiene para preferencias y el registro de migración. No es el
almacenamiento principal de CV. Firestore conserva una caché local por diseño.

## Validación

```powershell
.\gradlew.bat :core-data:testDevDebugUnitTest :features:resume:testDevDebugUnitTest :app:compileDevFreeDebugKotlin
cd firebase/rules-tests
npm ci
npm test
```

Las pruebas de reglas usan el emulador con un proyecto `demo-`, sin credenciales
ni datos reales. Comprueba también dos dispositivos del mismo usuario, edición
sin conexión y reconexión, cierre de sesión, migración interrumpida y permisos
rechazados antes de publicar la app.

Referencias oficiales:
- https://firebase.google.com/docs/firestore/manage-data/enable-offline
- https://firebase.google.com/docs/firestore/manage-data/transactions
- https://firebase.google.com/docs/firestore/security/rules-conditions
