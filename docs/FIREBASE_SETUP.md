# Firebase y arranque de PagoControl

## Estructura aplicada desde AndroidBase

`app/AndroidApplication` inicializa Timber, `AppAnalytics`, App Check y Koin en ese orden. `AppModule` ensambla los módulos propios de PagoControl. `MainActivity` concentra el tema, las barras del sistema y la creación del controlador de navegación. `MainGraph` ensambla los destinos y observa `AuthSessionViewModel`, que refresca el perfil al volver a primer plano.

La integración conserva los módulos y el diseño de PagoControl. No incorpora los planes de acceso, facturación, currículums ni la comprobación de actualizaciones de CV Free. Los botones de Google y huella mantienen su comportamiento anterior y quedan pendientes de implementación.

El paquete principal de `app` contiene únicamente `AndroidApplication`, `AppModule`, `MainActivity` y `MainGraph`. La carpeta `ui` replica los componentes de AndroidBase con los colores y tipografía de PagoControl: `LogoutConfirmationDialog`, `MainScreenBackHandler`, `ForceUpdateScreen`, `ForceUpdateColors` y `UpdateState`. La pantalla de actualización queda preparada, sin bloquear el arranque ni consultar Play Store hasta implementar ese flujo. Atrás en las cuatro secciones principales solicita confirmar el cierre de sesión; las pantallas secundarias conservan su navegación habitual.

Las integraciones Firebase de Analytics y App Check viven en `core-platform`; `AndroidApplication` solo las configura e inicializa. No se agregan otras carpetas de AndroidBase dentro de `app`.

`features/auth` contiene:

- `domain/entity`: identidad autenticada (`AuthUser`).
- `domain/repository`: contrato de autenticación.
- `domain/usecase`: acceso, registro, recuperación, sesión, perfil y actualización de nombre.
- `domain/exception` y `domain/mapper`: fallos tipados, separados entre validación y operación.
- `data/datasource`: Firebase Auth y el documento `users/{uid}` de Firestore.
- `data/repository`: adaptación de errores de Firebase y puente a los contratos compartidos de `session`.
- `data/security`: límite de tres intentos por acción en quince minutos, persistido como en AndroidBase.
- `ui`: ViewModels conectados a los casos de uso, conservando las pantallas, campos y callbacks de PagoControl.

Los contratos compartidos permiten que Inicio y Perfil usen la sesión sin depender directamente de la feature Auth. Los eventos de Analytics describen operaciones y pantallas, sin correos ni contraseñas; las excepciones se sanitizan antes de enviarlas a Crashlytics. La colección de Analytics y Crashlytics está desactivada en debug y habilitada en release.

## Configuración del proyecto Firebase

`app/google-services.json` corresponde a `com.devpaul.pagocontrol`. La compilación normal lo utiliza con los plugins Google Services y Crashlytics y el BoM de Firebase centralizado en el catálogo.

En Firebase Console:

1. Habilitar Authentication → Email/Password.
2. Crear la base Cloud Firestore.
3. Revisar y publicar las reglas de `firebase/firestore.rules`; cada cuenta solo puede leer y actualizar su propio perfil.
4. Configurar App Check para Android: proveedor debug durante desarrollo y Play Integrity en release. Registrar el token debug antes de exigir App Check en desarrollo.
5. Habilitar Analytics en el proyecto si se van a consultar los eventos de release.

No se publican reglas ni se modifican servicios remotos automáticamente. App Check incluye el proveedor adecuado por variante; su registro y la política de enforcement se gestionan en la consola.

## Comportamiento de autenticación

Login exige correo válido y contraseña no vacía. Registro exige nombre de 2 a 80 caracteres, correo válido, contraseña de al menos ocho caracteres y aceptación del consentimiento que ya muestra la UI. Firebase restaura la sesión del dispositivo; cerrar sesión limpia la navegación autenticada.

El registro completa el nombre y el documento de Firestore antes de publicar la nueva sesión. Si falla esa fase, se cierra la sesión y se permite recuperar la cuenta iniciando sesión otra vez; no se elimina automáticamente la cuenta creada. Las contraseñas no se guardan en DataStore ni en SavedStateHandle.

Recuperación utiliza el enlace de Firebase por correo y confirma el resultado sin revelar si existe una cuenta. Perfil actualiza el nombre en Firebase; el correo autenticado es de solo lectura. Las preferencias de apariencia, notificaciones y valoración siguen siendo locales. Los movimientos y reportes continúan siendo ilustrativos.

## Pruebas locales con Firebase Emulator Suite

Requiere JDK 21 y Node.js. En una terminal:

```powershell
npx --yes firebase-tools@15.35.0 emulators:start --only auth,firestore --project demo-pagocontrol
```

Compilar para el emulador Android:

```powershell
.\gradlew.bat '-PfirebaseEmulatorHost=10.0.2.2' :app:assembleDebug :app:assembleDebugAndroidTest
```

Esta configuración debug utiliza una instancia Firebase separada con el proyecto `demo-pagocontrol`, sin cuentas ni correos de producción. Auth escucha en 9099 y Firestore en 8080. No se instala App Check ni se recolecta telemetría en esta configuración. Release siempre utiliza la configuración normal.

`AuthHomeFlowTest` y `AccountScreensFlowTest` solo ejecutan operaciones de cuenta con el modo de emuladores habilitado. Sus preparaciones reinician únicamente el contador local de intentos del dispositivo de pruebas para mantener independencia entre casos.

Validación:

```powershell
.\gradlew.bat :features:auth:testDebugUnitTest :core-data:testDebugUnitTest :features:profile:testDebugUnitTest
.\gradlew.bat :app:assembleDebug :app:compileReleaseKotlin :app:lintDebug
```
