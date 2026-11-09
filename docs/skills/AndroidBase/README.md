# Android Base

Proyecto inicial extraído de los patrones de CvFree, con **cuatro features de ejemplo: `splash`, `onboarding`, `auth` y `home`**. La base ejecutable conserva Kotlin, Compose, Material 3, arquitectura por capas, módulos Gradle, Koin Annotations/KSP, rutas tipadas, coroutines, DataStore e infraestructura Firebase/HTTP.

## Empezar en cinco pasos

1. Extrae `AndroidBase/` en una carpeta propia. Abre **esa carpeta** en Android Studio, no el repositorio CvFree.
2. Selecciona JDK 21 como Gradle JDK e instala el SDK indicado en `buildSrc/src/main/kotlin/Config.kt` (compileSdk 37).
3. Sincroniza Gradle. Android Studio crea tu `local.properties`; no contiene rutas de la computadora donde se generó la base.
4. Selecciona la variante **devDebug** y ejecuta `app`.
5. Avanza u omite la introducción; pulsa **Iniciar sesión** para entrar directamente en modo local, sin credenciales, guarda ejemplos, cambia el tema y cierra sesión. Puedes empezar a sustituir las features sin configurar un backend.

```powershell
.\gradlew.bat :app:assembleDevDebug
.\gradlew.bat :app:installDevDebug
```

En macOS/Linux: `chmod +x gradlew` y usa `./gradlew`. Necesitas acceso a repositorios de dependencias y los componentes Android SDK correspondientes. La biblioteca de skills está incluida en `skills/`; consulta su `README.md` para elegir la que corresponda.

## Qué funciona inicialmente

| Capacidad | Estado |
| --- | --- |
| Splash y onboarding | Marca, lectura de preferencias y tres pasos; finalización persistida en DataStore |
| Auth demo | Login, Registro y recuperación en carpetas propias; sesión temporal y formularios sin correos/cuentas reales |
| Home demo | Lista y guardado local con DataStore; datos conservados al reiniciar |
| Apariencia | Tema claro/oscuro persistente y compartido por toda la app |
| Idiomas | Español por defecto e inglés según idioma del dispositivo |
| Navegación | Rutas serializables; cambia de grafo por identidad y elimina la UI protegida al salir |
| Estado | UiState + intenciones con ViewModel, StateFlow y lifecycle |
| DI | Koin, anotaciones, KSP, módulos por feature y composición en Application |
| Firebase | Adaptadores reales Auth/Firestore, App Check por build type, Analytics, Crashlytics y Remote Config, activables con propiedad |
| HTTP | OkHttp + Retrofit registrados en DI, sin solicitudes a un servicio ficticio |
| Backend | Callable autenticada de ejemplo, reglas completas, índices y pruebas |
| Entrega | Variantes dev/prod, firma externa, R8, CI y scripts de comprobación |

En modo demo no se inicializa Firebase y no hay telemetría remota. La sesión demo no valida credenciales, no crea cuentas ni envía correos. Todos los accesos demo usan el mismo espacio local `demo`, destinado únicamente a exploración. Los ejemplos locales no se migran automáticamente a Firebase.

## Arquitectura y módulos

```text
app                      Composición, Application, Activity y MainGraph
features/splash          Arranque y preferencias iniciales
features/onboarding      Introducción persistente de tres pasos
features/auth            ui/login, ui/register y ui/forgotpassword
features/home            Lista, creación de ejemplos y preferencia de tema
core-domain              Entidades, resultados, contratos y AddExampleUseCase
session                  Contrato de identidad y estados de sesión, Kotlin puro
core-data                DataStore, Firebase, adaptadores y configuración Retrofit
core-platform            BaseViewModel y contrato de observabilidad
core-designsystem        Tema Material 3, colores y tipografía
navigation               Contratos sellados por feature en route/, Kotlin/JVM
buildSrc                 Convenciones de app, librería Android/Kotlin y feature
firebase                 Functions, reglas completas y pruebas de reglas
skills                   Las skills de guía para extender el proyecto
```

```mermaid
flowchart TD
    App[app] --> Features[splash + onboarding + auth + home]
    App --> Data[core-data]
    Features --> Domain[core-domain]
    Features --> Session[session]
    Features --> UI[core-designsystem]
    Features --> Platform[core-platform]
    Features --> Routes[navigation]
    Data --> Domain
    Data --> Session
    Session --> Domain
```

Las features no dependen de `core-data` ni entre sí. El dominio y los contratos de sesión no importan Android/Firebase; solo SessionModule incorpora anotaciones Koin para la composición. La capa de datos convierte excepciones en resultados y propaga cancelación. El flujo protegido obtiene el UID desde la sesión real; no desde un argumento externo. Las reglas del servidor siguen siendo la autoridad de acceso.

## Inicializacion de Koin

`AndroidApplication.onCreate` prepara Timber/Firebase segun la configuracion y llama a `koinInitializer()`. El arranque registra contexto Android, logger y la propiedad `firebase.enabled`, y carga exclusivamente `AppModule().module`.

`AppModule` usa `@Module(includes = [CoreDataModule::class, NavigationModule::class, SessionModule::class, NetworkModule::class, AuthModule::class, HomeModule::class, SplashModule::class, OnboardingModule::class])` y `@ComponentScan`. Los modulos de cada feature mantienen esas anotaciones; los ViewModels usan `@KoinViewModel`, y los proveedores compartidos `@Single`. App, datos, navigation, session y las features aplican `plugin.koin` para generar sus definiciones con KSP.

El modo demo no resuelve los proveedores Firebase: son dependencias diferidas y protegidas por la configuracion. `SessionLifecycle` conserva la limpieza del listener Auth al cerrar Koin, mediante una extension registrada despues del arranque. Esta base no modifica archivos generados ni combina registros DSL con los de anotaciones.

El registro de esa extension queda aislado en `koinInitializer` con `@OptIn(KoinInternalApi::class)`, requerido por el ExtensionManager de Koin 4.2.2. Al cambiar esa version debe revisarse este punto de lifecycle; no se elimina el cierre del listener para adaptar la sintaxis de DI.

## Extender las features

Conserva los límites por responsabilidad: Splash prepara el arranque; Onboarding explica la aplicación; Auth agrupa las pantallas de cuenta; Home contiene el flujo principal.

1. Define modelos, contratos y reglas en `core-domain`; implementa proveedores en `core-data`.
2. Cambia `LoginUiState/LoginUiIntent/LoginError/LoginViewModel` en `auth/ui/login/` o `HomeUiState/HomeUiIntent/HomeViewModel` en `home/ui/home/` según el flujo. Cada tipo de nivel superior tiene su propio archivo; el ViewModel contiene únicamente su lógica. Conserva el aislamiento de SDKs.
3. Actualiza recursos de cada feature y crea componentes compartidos en `core-designsystem` cuando sean reutilizables.
4. Expón destinos desde `navigation` y registra los grafos desde `app/MainGraph.kt`.
5. Si agregas una feature nueva, aplica `plugin.feature`, define namespace y crea su módulo anotado. Regístrala en `settings.gradle.kts`, dependencias de `app` y `AndroidApplication`.
6. Actualiza o sustituye los ejemplos y sus pruebas. No hay módulos de CV, plantillas de currículum ni datos del proyecto original.

La autenticación ejemplifica orquestación simple sobre el contrato de sesión. Añade casos de uso específicos si aparecen reglas de registro, perfil, permisos o múltiples pasos. `AddExampleUseCase` muestra validación de negocio fuera del ViewModel.

### Archivos por pantalla

```text
features/auth/.../features/auth/
  AuthGraph.kt
  AuthModule.kt
  ui/login/
    LoginScreen.kt
    LoginViewModel.kt
    LoginUiState.kt
    LoginUiIntent.kt
    LoginError.kt
  ui/register/
    RegisterScreen.kt
    RegisterViewModel.kt
    RegisterUiState.kt
    RegisterUiIntent.kt
    RegisterError.kt
  ui/forgotpassword/
    ForgotPasswordScreen.kt
    ForgotPasswordViewModel.kt
    ForgotPasswordUiState.kt
    ForgotPasswordUiIntent.kt
    ForgotPasswordError.kt
features/home/.../features/home/
  HomeGraph.kt
  HomeModule.kt
  ui/home/
    HomeScreen.kt
    HomeViewModel.kt
    HomeUiState.kt
    HomeUiIntent.kt
```

Los nombres de clase coinciden con los archivos y usan el prefijo de su pantalla. `LoginError` traduce los fallos de dominio para la UI de acceso. Las intenciones conservan sus variantes anidadas dentro del contrato sealed. Las pruebas siguen los paquetes `ui/login` y `ui/home`. La base Home conserva su ejemplo de lista; no necesita el contrato `HomeContent` del dashboard de PagoControl.

## Flujo inicial y referencias visuales

La UI se inspira en los diseños de Stitch presentes en `docs/stitch_pagocontrol_android_app_ui` de PagoControl: Splash, tres páginas de introducción y los tres formularios de Auth. La plantilla utiliza marca AndroidBase, la paleta amarilla compartida y Manrope local con su licencia. Los previews cubren claro y oscuro.

Splash carga `StartupPreferences` y muestra la marca durante 650 ms sin bloquear el hilo principal. Si no hay sesión y es la primera apertura, entra en Onboarding; en aperturas posteriores va a Login. Una sesión válida lleva a Home. El arranque espera que el proveedor de sesión deje de estar en Loading. Registro y recuperación pertenecen al grafo Auth; el cambio de identidad limpia el historial protegido.

La preferencia de introducción usa DataStore `startup` y `onboarding_completed=false` por defecto. Se guarda antes de salir de Onboarding y no se elimina con logout. Cada paso puede avanzar o volver; omitir, el enlace de acceso y el último botón llevan a Login. Registro se abre únicamente desde Login.

Los formularios tienen contratos independientes y nunca persisten contraseñas. En modo local, Iniciar sesión abre Home sin completar campos; Firebase mantiene validación y autenticación real. `AccountRepository` y `SessionAccountRepository` conectan registro/recuperación con el proveedor. En demo muestran expresamente que no se crean cuentas ni se envían mensajes. En Firebase usan el contrato real de sesión; recuperación utiliza un enlace por correo, sin prometer un OTP de seis dígitos. La captura de nombre aún no crea un perfil remoto.

Google, biometría, términos de servicio y políticas definitivas siguen pendientes de integración. Las tarjetas del onboarding son ilustrativas y no acreditan sincronización bancaria ni cifrado local implementados.

## Identidad y versiones del nuevo producto

- Nombre de proyecto: `settings.gradle.kts`.
- Nombre visible: recursos de `app/src/main/res/values/strings.xml` y `app/src/dev/res/values/strings.xml`.
- ID y namespace: `app/build.gradle.kts`; los namespaces de las librerías se definen en sus builds. Usa refactorización de paquetes de Android Studio para cambiar `com.example.androidbase` en todo el código.
- SDK/JDK: `buildSrc/src/main/kotlin/Config.kt`.
- Versiones de librerías/plugins: `gradle/libs.versions.toml`. Las de buildSrc se obtienen del mismo catálogo.
- Versión del producto: `app.versionCode` y `app.versionName` en `gradle.properties` o propiedades CI.
- API propia: `api.baseUrl` debe ser HTTPS y terminar en `/`. El valor `https://example.invalid/` es deliberadamente inactivo. Agrega tus interfaces Retrofit y registra servicios cuando exista un contrato real.

El toolchain se basa en el del repositorio de origen: Gradle 9.7.1, AGP 9.3.2, Kotlin 2.4.10 y Java 21. Se usa Kotlin integrado de AGP para módulos Android y plugin JVM para módulos puros. No se copian dependencias Firebase KTX antiguas: Firebase usa BOM y artefactos principales.

## Conectar tu Firebase

1. Crea proyectos Firebase propios para desarrollo y producción y habilita Email/Password en Auth y una base Firestore.
2. Registra `com.example.androidbase.dev` para dev y `com.example.androidbase` para prod, o tus IDs definitivos.
3. Coloca cada archivo descargado en `app/src/dev/google-services.json` y `app/src/prod/google-services.json`. No se distribuye un archivo ficticio ni uno de CvFree.
4. Ejecuta `:app:assembleDevDebug -Pfirebase.enabled=true`. Para activarlo siempre en tu copia, cambia `firebase.enabled=true` en `gradle.properties`.
5. Publica las reglas de **tu proyecto nuevo** desde `firebase/` con proyecto explícito, después de ejecutar las pruebas. Revisa e integra antes de desplegar si tu base ya tiene datos o reglas propios.
6. Registra App Check: proveedor Debug para debug y Play Integrity para release. Registra el token debug local sin compartirlo. Habilitar enforcement en consola es una configuración externa.
7. Verifica login, registro, recuperación y aislamiento entre dos UIDs. La lista cambia a Firestore al activar Firebase; el modo cloud nunca cae silenciosamente a demo si el servicio falla.

`firebase.enabled` es una opción **de build**, no un selector de cuenta ni un permiso. Sus archivos Firebase deben coincidir con el ID de cada variante que compiles. En debug Analytics y Crashlytics quedan desactivados por defecto; release los activa cuando se habilita Firebase. Ajusta esta política a las decisiones de privacidad/consentimiento de tu producto antes de distribuir.

Remote Config incluye fetch y el default `example_enabled=true` como infraestructura; ninguna regla de negocio depende todavía de ese parámetro. Crashlytics captura fallos no controlados cuando se activa; los errores controlados del ejemplo envían únicamente un código de operación a Analytics. No se registran correos, contraseñas ni títulos.

## Variantes

| Variante | ID | Uso |
| --- | --- | --- |
| devDebug | `com.example.androidbase.dev` | Desarrollo local y pruebas |
| devRelease | `com.example.androidbase.dev` | Comprobar optimización en desarrollo |
| prodDebug | `com.example.androidbase` | Diagnóstico con configuración de prod propia |
| prodRelease | `com.example.androidbase` | Candidato de distribución |

Las librerías no duplican sabores que no usan. Se retira la dimensión Free/Premium porque el nuevo producto todavía no tiene planes comerciales. Si la necesitas, consulta la skill de Gradle y define una matriz explícita.

## Pruebas y CI

```powershell
.\gradlew.bat :core-domain:test :core-data:testDebugUnitTest :features:auth:testDebugUnitTest :features:home:testDebugUnitTest :features:splash:testDebugUnitTest :features:onboarding:testDebugUnitTest
.\gradlew.bat :app:lintDevDebug :app:assembleDevDebug :app:compileDevDebugAndroidTestKotlin
.\gradlew.bat :app:connectedDevDebugAndroidTest
npm --prefix firebase/functions ci
npm --prefix firebase/functions test
npm --prefix firebase/rules-tests ci
npm --prefix firebase/rules-tests test
```

Las instrumentadas necesitan un emulador/dispositivo conectado; las pruebas de reglas necesitan Java 21 y Emulator Suite. Las pruebas unitarias backend no prueban por sí solas Auth/App Check desplegados. `.github/workflows/verify.yml` automatiza build/lint/pruebas locales y reglas en GitHub Actions; no publica ni incorpora secretos de producción. La ejecución remota se comprueba después de subir tu repositorio.

Consulta `docs/VALIDATION.md` para el resultado real de las comprobaciones realizadas al generar este paquete. No confundas una tarea definida con una tarea ejecutada.

## Firma y despliegue

Copia `keystore.properties.example` como `keystore.properties` en tu copia y completa rutas/contraseñas locales. Las claves y propiedades están excluidas de Git y del empaquetado. Sin esa configuración se pueden compilar builds release sin firma de distribución; no están listos para subir a Play.

```powershell
.\gradlew.bat :app:bundleProdRelease -Pfirebase.enabled=true
```

Generar el AAB no lo publica. Configura Google Play Console, firma y políticas de tu producto antes de entregar. Para Firebase consulta `firebase/README.md`. No se han creado proyectos, cuentas, claves, secretos, productos comerciales ni despliegues externos con esta plantilla.

## Capacidades opcionales del proyecto original

PDF/OCR, IA, Billing y push tienen skills específicas incluidas, pero no se activan como funcionalidades de estas features de ejemplo. Integra cada una cuando su contrato de producto esté definido; copiar IDs de suscripción, cupos o prompts de CvFree introduciría reglas ajenas al nuevo negocio. Los adaptadores de infraestructura incluidos están descritos con su estado real en la tabla inicial.

## Referencias de compatibilidad

- [Kotlin integrado en AGP](https://developer.android.com/build/migrate-to-built-in-kotlin).
- [Firebase BOM y módulos Kotlin](https://firebase.google.com/docs/android/learn-more).
- [Koin Annotations](https://insert-koin.io/docs/reference/koin-annotations/start/).

## Contratos de navegacion por feature

`navigation/route/` contiene los contratos de nivel superior `AppRoute`, `GraphRoute : AppRoute` y `DestinationRoute : AppRoute`, en ese orden y juntos en `AppRoute.kt`. `AuthRoute`, `HomeRoute`, `SplashRoute` y `OnboardingRoute` son interfaces selladas con objetos serializables anidados:

- `AuthRoute.Graph`: `Login`, `Register`, `ForgotPassword`.
- `HomeRoute.Graph`: `Home`.
- `SplashRoute.Graph`: `Splash`.
- `OnboardingRoute.Graph`: `Welcome` (tres pasos).

Cada feature usa su contrato en `navigation<FeatureRoute.Graph>` y `composable<FeatureRoute.Destino>`. `MainGraph` conecta estos grafos y conserva las reglas de sesion, restauracion y limpieza del historial. No hay rutas planas paralelas ni dependencias entre features.

`NavigationModule` y `SessionModule` se declaran con `@Module` y `@ComponentScan` y se incluyen explicitamente en `AppModule`. Son puntos de composicion de contratos compartidos; no registran rutas, modelos ni interfaces sin implementacion como singletons. Los repositorios concretos de sesion siguen registrados una sola vez en `CoreDataModule`. Los contratos, estados y modelos de sesion tienen archivos independientes.

Los flujos de Splash, Onboarding, Login, Registro y recuperacion se presentan en espanol, incluidos errores y apariencia, tambien cuando el sistema esta en ingles. Los recursos predeterminados `values/` son la fuente de estos textos; no hay traducciones inglesas activas para estos flujos. Splash utiliza un degradado adaptable al tamano con halo dorado superior/inferior y acento verde lateral, manteniendo contraste en ambos temas.

## Argumentos con nombre

En llamadas Kotlin con tres o mas argumentos suministrados, usa nombres explicitos para todos los argumentos dentro de los parentesis: `state = state`, `onIntent = viewModel::onIntent`, etc. Aplica tambien a constructores, inicializacion de propiedades/variables, previews, pruebas, grafos, pantallas y componentes compartidos. No cambia los nombres de los parametros declarados ni el comportamiento de la funcion.

Conserva la lambda final fuera de los parentesis cuando sea idiomatico en Compose; cuenta como argumento al decidir el umbral. En llamadas largas, especialmente pantallas desde los grafos, coloca un argumento por linea. Con uno o dos argumentos, los nombres son opcionales.

No fuerces nombres donde Kotlin no los admite (APIs Java o llamadas a valores de tipo funcion). Mantén los elementos `vararg` posicionales, como `listOf`, `arrayOf`, `FontFamily` o claves multiples de `LaunchedEffect`; no conviertas listas en arrays ni agregues adaptadores solo para cumplir la regla.

```kotlin
LoginScreen(
    state = state,
    onIntent = viewModel::onIntent,
    themeIndex = themeIndex,
    onThemeSelected = onThemeSelected,
    onRegister = onRegister,
    onForgotPassword = onForgotPassword,
)
```

## Ajuste visual de entrada

Splash sigue `splash_screen_pagocontrol_modo_oscuro`: fondo carbon con halos dorados y verde lateral, emblema de cartera dorado, distintivo PE, fuentes en capsulas y pie de privacidad sin prometer cifrado no implementado. El primer onboarding sigue `onboarding_1_detecci_n_autom_tica_modo_oscuro`, con tarjetas de ejemplo, iconos de proveedor morado/celeste y simbolos/etiquetas blancos en ambos temas. `EntryColors` centraliza esta paleta; los colores de proveedor admiten texto blanco con contraste >= 4.5:1, mientras el texto general se adapta al tema. Las tarjetas y etiquetas pueden envolver contenido y mantienen scroll.

Al terminar los tres pasos se guarda la preferencia y se abre Login, nunca Registro. Las pruebas verifican ese destino y el bloqueo de envios repetidos; Registro sigue disponible desde el acceso.

## Referencias oscuras de onboarding 2 y 3

Los pasos 2 y 3 incorporan `onboarding_2_privacidad_y_seguridad_modo_oscuro` y `onboarding_3_control_y_alertas_modo_oscuro` de Stitch. El segundo usa orbitas discontinuas, candado central, distintivos de fuentes de colores y tarjetas de privacidad. El tercero presenta saldo, Netflix, Luz del Sur y una tarjeta de ahorro con progreso. `PrivacyIllustration` y `ControlIllustration` estan en `ui/onboarding/components/`; ambos tienen previews claros y oscuros. Se conservan las etiquetas de ejemplo y no se afirman cifrado Keystore, funcionamiento offline o lectura SMS que aun no esten implementados. El ultimo boton sigue abriendo Login.

## Estilo de enlaces y proveedores de Auth

`AccountLink`, en core-designsystem, centra el texto de contexto y destaca solo la accion: amarillo y negrita en oscuro; texto oscuro, negrita y subrayado en claro. Se reutiliza en Login, Registro, recuperacion y el acceso desde Onboarding. Los textos completos siguen en los recursos de cada feature y el area tactil mantiene un minimo de 48 dp.

El boton de Google conserva el simbolo multicolor como vector local sin tintado. La huella usa un simbolo verde sobre circulo menta en claro y verde profundo en oscuro; sus contenedores son claros de dia y oscuros con borde de noche. Estos cambios son visuales: no habilitan OAuth ni biometria real. Registro separa el checkbox y el texto de terminos con 12 dp y agrega espacio vertical. No se modifica el flujo de recuperacion ni se implementa OTP al adoptar sus referencias oscuras.
