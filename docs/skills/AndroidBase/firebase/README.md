# Backend de Android Base

Este directorio pertenece a tu proyecto nuevo. Usa siempre `--project` con su ID explícito. Las reglas son **completas** para el esquema de ejemplo; a diferencia de los fragmentos de CvFree, pueden probarse como unidad. Si se integran con una base ya existente, revisa reglas y colecciones antes de publicar.

## Preparar y probar

Requiere Node 22. Las pruebas del emulador también requieren Java 21.

```powershell
npm --prefix functions ci
npm --prefix functions test
npm --prefix rules-tests ci
npm --prefix rules-tests test
```

`users/{uid}/items/{itemId}` admite únicamente `title`, string entre 1 y 120 caracteres. Solo el UID propietario puede operar. El resto de rutas se deniega. El cliente normaliza espacios; las reglas imponen tipo, campos y tamaño.

## Desplegar cuando corresponda

Instala o ejecuta Firebase CLI en tu entorno, autentícate y elige un proyecto tuyo con los servicios requeridos habilitados.

```powershell
firebase deploy --only firestore:rules,firestore:indexes --project TU_PROJECT_ID
firebase deploy --only functions:base --project TU_PROJECT_ID
```

El runtime del backend es Node 22 y su región `us-central1`. `getServerStatus` exige sesión y App Check y devuelve estado/hora del servidor sin datos de usuario. Es una callable de infraestructura; la UI no necesita invocarla para funcionar.

Para consumirla desde un repositorio nuevo, usa el `FirebaseFunctions` registrado en DI y el nombre `getServerStatus`. Mantén el SDK fuera del dominio. Para secretos futuros utiliza Secret Manager; esta función de ejemplo no necesita ninguno.

## Verificar tras desplegar

Confirma proyecto/región, login y datos desde Android, rechazo de accesos de otro UID y que el callable rechace solicitudes sin autorización. Registrar el token debug de App Check y habilitar enforcement son pasos de consola independientes. No se despliegan cambios desde el pipeline de validación incluido.
