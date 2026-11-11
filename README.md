# PagoControl

Base Android modular con Kotlin, Jetpack Compose, MVVM/MVI, Koin y navegación tipada.

Estructura basada en `docs/skills/AndroidBase` y la skill `android-modular`: `app`, `buildSrc`, `core-domain`, `core-data`, `core-designsystem`, `core-platform`, `navigation`, `session` y las features `splash`, `onboarding`, `auth`, `home`, `movements`, `reports` y `profile`. Las convenciones Gradle se centralizan en `buildSrc/src/main/kotlin/plugin/`.

Implementa Login, Registro y recuperación por correo con Firebase Authentication y perfiles en Cloud Firestore. El arranque sigue AndroidBase con Timber, Analytics/Crashlytics, App Check y Koin, conservando la UI de PagoControl y sus temas claro, oscuro y del sistema. Inicio, Movimientos y Reportes mantienen datos ilustrativos.

## Ejecutar

Abrir en Android Studio, configurar Android SDK 37 y JDK 21 y ejecutar `app`, o generar el APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

En la primera apertura puedes avanzar u omitir la introducción. Crea una cuenta con correo y contraseña o inicia sesión con una cuenta existente. Google y huella quedan pendientes. Consulta [la configuración de Firebase y las pruebas con emuladores](docs/FIREBASE_SETUP.md); las cuentas bancarias no están conectadas.

Consulta [la arquitectura y validaciones](docs/ARCHITECTURE.md) y [las skills de referencia](docs/skills/README.md).
