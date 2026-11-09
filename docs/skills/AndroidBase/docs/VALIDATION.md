# Validación del paquete

Proyecto generado a partir de CvFree el 26 de octubre de 2026. Se comprobó la configuración independiente de esta plantilla, sin compilar ni modificar las features del proyecto original.

## Comprobaciones realizadas

| Comprobación | Resultado |
| --- | --- |
| `:app:assembleDevDebug` | APK debug generado |
| `:app:bundleProdRelease` | AAB generado con R8 y reducción de recursos, sin firma de distribución propia |
| `:core-domain:test` | 3 pruebas aprobadas |
| `:core-data:testDebugUnitTest` | 2 pruebas aprobadas |
| `:features:auth:testDebugUnitTest` | 1 prueba aprobada |
| `:features:home:testDebugUnitTest` | 1 prueba aprobada |
| `:app:compileDevDebugAndroidTestKotlin` | Código de prueba instrumentada compilado |
| `:app:lintDevDebug` | Sin errores; avisos de versiones de dependencias disponibles |
| Lógica Functions con `node --test` | 2 pruebas aprobadas |
| Reglas Firestore con Emulator Suite | 3 pruebas aprobadas |

Entorno de comprobación: Windows, JDK 21, Node 22, Android SDK 37 y wrapper Gradle del proyecto. Las pruebas de reglas fijan Firebase JS 12.4.0 y rules-unit-testing 5.0.0 para mantener compatibilidad con Node 22; no reproducen la combinación del origen que exige una versión superior de Node para esas herramientas.

## Límites comprobados

- La ejecución de la prueba instrumentada en Android queda pendiente: el emulador disponible solicitó autorización ADB. Se cerró el emulador de comprobación; no se afirma un recorrido visual ejecutado.
- No se activó Firebase contra un proyecto real ni se incorporó `google-services.json`. Auth, Firestore, App Check y recepción remota de telemetría necesitan la configuración del nuevo propietario y pruebas de integración.
- El AAB validado usa `firebase.enabled=false`. No está firmado con una clave de distribución del propietario y no se publicó en Play.
- No se desplegaron Functions ni reglas a servicios externos. El emulador comprueba reglas locales, no permisos de un proyecto remoto.
- El archivo de CI está incluido, pero su ejecución en GitHub requiere subir el nuevo repositorio.
- No se incorporan APK/AAB, cachés, dependencias Node instaladas ni credenciales dentro del ZIP. Se incluyen fuentes, wrapper, lockfiles, documentación y skills.

## Actualización de contratos por pantalla — 8 de noviembre de 2026

Se alinearon nombres, packages y archivos de las features con la convención de PagoControl:

- Login: `LoginScreen`, `LoginViewModel`, `LoginUiState`, `LoginUiIntent` y `LoginError` en `auth/ui/login/`.
- Home: `HomeScreen`, `HomeViewModel`, `HomeUiState` y `HomeUiIntent` en `home/ui/home/`.
- Cada contrato de nivel superior tiene su archivo; los ViewModels contienen su lógica. Las variantes anidadas permanecen dentro del sealed correspondiente.
- Las pruebas reflejan los paquetes por pantalla. Navegación e inyección Koin se actualizaron; KSP regenera sus referencias.
- Las skills `android-modular`, `android-mvi`, `android-feature` y `android-compose-ui` están sincronizadas con la biblioteca de `docs/skills` de PagoControl; las ocho copias superaron `quick_validate.py`.

Verificación ejecutada con el wrapper de esta plantilla y Firebase desactivado:

```powershell
.\gradlew.bat :features:auth:testDebugUnitTest :features:home:testDebugUnitTest :app:assembleDevDebug :app:lintDevDebug :app:compileDevDebugAndroidTestKotlin
```

Resultado: build correcto, una prueba de Login y una de Home aprobadas, lint sin errores y prueba instrumentada compilada. En esta actualización no se ejecutó la prueba instrumentada en dispositivo, release ni pruebas de backend; los resultados de esas áreas en la tabla inicial son históricos.

## Actualización del arranque Koin — 8 de noviembre de 2026

Se replicó el patrón de PagoControl: `AndroidApplication.koinInitializer()` registra contexto y logger y carga `AppModule().module`. El módulo raíz usa `@Module(includes = [...])` y `@ComponentScan` para incluir `CoreDataModule`, `NetworkModule`, `AuthModule` y `HomeModule`. Los ViewModels usan `@KoinViewModel`; los proveedores compartidos usan `@Single` y el caso de uso conserva `@Factory`. `plugin.koin` centraliza KSP en app, datos y features.

Se conserva la selección demo/Firebase mediante la propiedad de build `firebase.enabled`, con dependencias Firebase diferidas. `SessionLifecycle` registra el cierre del listener cuando se cierra Koin; su registro usa el opt-in aislado de ExtensionManager correspondiente a Koin 4.2.2. No se incorporan claves ni se activa un proyecto Firebase real.

Comprobaciones ejecutadas:

```powershell
.\gradlew.bat :core-data:testDebugUnitTest :features:auth:testDebugUnitTest :features:home:testDebugUnitTest :app:assembleDevDebug :app:lintDevDebug :app:connectedDevDebugAndroidTest
```

Resultado: APK devDebug generado, cuatro pruebas unitarias aprobadas y recorrido demo de acceso, guardado y logout aprobado en Pixel 7 / Android 16. La prueba de UI espera mediante una condición visible el regreso a Login tras logout. Lint termina sin errores y con doce advertencias de dependencias disponibles. Las copias de `android-koin` en ambas bibliotecas se sincronizaron y superaron `quick_validate.py`.

La integración cloud, el cierre del listener con Firebase real, release y backend no se ejecutaron en esta actualización. Los resultados iniciales de esas áreas son históricos.

## Repetir las comprobaciones

Desde PowerShell, con JDK/SDK y Node configurados:

```powershell
.\scripts\verify.ps1
```

Con un dispositivo autorizado conectado, agrega `-Instrumented`. Para verificar la distribución con servicios reales, configura primero Firebase y firma según el README; esa verificación es distinta de la del modo demo.
