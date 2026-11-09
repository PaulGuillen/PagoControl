# Fotografía del proyecto de origen

Inspección local de CvFree realizada el 26 de octubre de 2026. Esta referencia describe archivos del repositorio, no certifica compatibilidad futura ni estado de servicios desplegados. Es portable: las rutas siguientes son evidencia histórica y no enlaces necesarios para ejecutar las skills.

## Stack observado

| Área | Evidencia local |
| --- | --- |
| Build | Gradle Kotlin DSL, wrapper y catálogo `gradle/libs.versions.toml` |
| Versiones declaradas | AGP 9.3.2, Kotlin 2.4.10, KSP 2.3.11, Compose BOM 2026.02.01, Koin 4.2.2, Firebase BOM 35.0.0 |
| Plataforma | `buildSrc/src/main/kotlin/Config.kt`: Java 21, minSdk 24, compileSdk/targetSdk 37 |
| Convenciones | Plugins `plugin.application`, `plugin.android.lib`, `plugin.kotlin.lib`, `plugin.feature` en `buildSrc` |
| UI | Compose, Material 3, ViewModels, UiState/UiIntent/UiEvent y Flow |
| DI | Koin Annotations, KSP y módulos generados |
| Navegación | Navigation Compose, rutas serializables tipadas y grafos por feature |
| Datos | Firebase Auth/Firestore, DataStore; Retrofit/OkHttp declarados como infraestructura disponible |
| Servicios | Functions JavaScript con Node 22, App Check, Analytics, Crashlytics y Remote Config |
| Capacidades específicas | PDFBox Android, ML Kit OCR/escáner, IA mediante Functions, infraestructura de Billing |
| Pruebas | JUnit, coroutines-test, instrumentadas Android/Compose, node:test y pruebas de reglas con emulador |

Las versiones son las declaradas, no el resultado de un build ejecutado durante esta documentación. El catálogo conserva entradas Firebase KTX con versiones individuales junto a un BOM; no copies esa combinación sin revisar la documentación oficial y las dependencias resueltas del siguiente proyecto.

## Módulos

- `app`: Application, Activity, registro DI, `MainGraph.kt` e integraciones globales.
- `core-domain`: Kotlin puro con modelos, contratos y resultados de dominio.
- `core-data`: repositorios, Firestore, codecs y migración de persistencia.
- `session`: contratos Kotlin puros de sesión.
- `navigation`: catálogo de rutas y contratos compartidos.
- `core-platform`: adaptadores Android, lifecycle, preferencias y observabilidad.
- `core-designsystem`: destino arquitectónico para tema y componentes visuales compartidos.
- `features`: splash, onboarding, auth, home, resume, settings y templates.
- `firebase`: Functions, fragmentos de reglas y pruebas de backend.

## Lo que enseña MainGraph

`app/src/main/java/com/devpaul/cvfree/MainGraph.kt` registra splash, onboarding, auth, home, resume y settings. Resuelve sesión y acceso, comparte un borrador, conecta callbacks, registra pantallas y limpia navegación al cerrar sesión. La navegación principal usa `launchSingleTop`, guardado y restauración de estado.

Para otro proyecto, conserva la composición de grafos en `app`, define el scope de los ViewModels compartidos y deja las operaciones del negocio en casos de uso/controladores. El autosave de un editor y sus flush deben tener dueño explícito; `ON_STOP` es un complemento, no garantía de persistencia frente a muerte del proceso.

## Diferencias que no se deben copiar sin pensar

- Auth contiene paquetes `data/domain/ui` dentro de una feature Android. Separar paquetes ayuda a organizar, pero no impone los límites que impondrían módulos Gradle separados.
- Existen archivos de tema en `core-platform/theme`; para el siguiente proyecto la responsabilidad visual corresponde a `core-designsystem` y la persistencia de apariencia a un adaptador.
- `plugin.feature` incorpora PDFBox para todas las features. En el siguiente proyecto limita dependencias específicas a los módulos consumidores.
- Algunos README del origen están desactualizados: el de navegación describe menos grafos que los registrados en `MainGraph`. Prioriza código y build real.
- Ambiente `dev/prod` se define en convenciones y acceso `free/premium` en `app`. Premium release está deshabilitado. No asumas que existe `assembleProdPremiumRelease`.
- El cambio de ambiente no añade actualmente un sufijo diferente a `applicationId`. Para coexistencia de instalaciones dev/prod, diseña identidades y registros Firebase coherentes.
- El plan Premium comercial aparece en construcción. Tener dependencia y fuente Billing no demuestra un circuito de compra publicado con verificación de servidor.
- `firebase/firebase.json` configura solamente Functions, codebase `resume-ai`. Los archivos `*.rules` de negocio son fragmentos para integrar, no reglas completas listas para reemplazar una base.
- No se observó carpeta `.github` con pipeline. CI y publicación automatizada en este paquete son propuestas para el próximo proyecto.

## Comandos de referencia

Desde la raíz del proyecto original:

```powershell
.\gradlew.bat :app:assembleDevFreeDebug
.\gradlew.bat :core-domain:test
.\gradlew.bat :features:auth:testDevDebugUnitTest
npm --prefix firebase/functions test
```

Las pruebas de reglas usan `npm test` desde `firebase/rules-tests`, requieren dependencias y herramientas del emulador. Estos comandos describen puntos de entrada existentes; esta generación documental no los ejecutó ni desplegó recursos.
