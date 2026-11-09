# PagoControl

Base Android modular con Kotlin, Jetpack Compose, MVVM/MVI, Koin y navegación tipada.

Estructura basada en `docs/skills/AndroidBase` y la skill `android-modular`: `app`, `buildSrc`, `core-domain`, `core-data`, `core-designsystem`, `core-platform`, `navigation`, `session` y `features/{auth,home}`. Las convenciones Gradle se centralizan en `buildSrc/src/main/kotlin/plugin/`.

Implementa **Auth y Home** con temas claro, oscuro y del sistema. Incluye dashboard de demostración, estados de carga/error, reintento y cierre de sesión.

## Ejecutar

Abrir en Android Studio, configurar Android SDK 37 y JDK 21 y ejecutar `app`, o generar el APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

En acceso, pulsar **Completar acceso de demostración** y luego **Ingresar a la demo**. No se conecta un proveedor real ni cuentas bancarias.

Consulta [la arquitectura y validaciones](docs/ARCHITECTURE.md) y [las skills de referencia](docs/skills/README.md).
