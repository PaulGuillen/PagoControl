# Base de PagoControl

## Alcance implementado

Solo dos features: `auth` y `home`. El acceso usa una cuenta demo en memoria y el dashboard contiene datos controlados inspirados en los diseños de `stitch_pagocontrol_android_app_ui`. No hay Firebase, servidor, lectura de SMS, permisos bancarios ni sincronización real. La interfaz lo indica expresamente. Auth no tiene diseño de referencia propio; utiliza el sistema visual documentado.

Cuenta demo: `demo@pagocontrol.pe` / `PagoControl123`. El formulario permite completar estos datos con un botón. No usar credenciales personales. La contraseña no se guarda en disco ni en `SavedStateHandle`. La sesión sobrevive a recreaciones de Activity, pero termina al morir el proceso.

## Módulos y dependencias

La estructura toma como referencia `docs/skills/AndroidBase` y `docs/skills/android-modular/SKILL.md`. Los módulos compartidos están en la raíz, como en esa base:

```text
app/
buildSrc/src/main/kotlin/plugin/
core-domain/
core-data/
core-designsystem/
core-platform/
navigation/
session/
features/auth/
features/home/
```

```mermaid
graph TD
    app --> auth[features:auth]
    app --> home[features:home]
    app --> data[core-data]
    app --> navigation[navigation]
    app --> design[core-designsystem]
    app --> domain[core-domain]
    app --> session[session]
    auth --> domain
    auth --> navigation
    auth --> design
    auth --> session
    auth --> platform[core-platform]
    home --> domain
    home --> navigation
    home --> design
    home --> session
    home --> platform
    data --> domain
    data --> session
```

| Módulo | Responsabilidad |
| --- | --- |
| `app` | Application, composición Koin, Activity, apariencia global y MainGraph |
| `core-domain` | Kotlin/JVM puro: validación, dashboard y contratos de repositorios |
| `core-data` | Implementaciones demo y preferencia de apariencia en DataStore |
| `navigation` | Kotlin/JVM puro: contratos de grafos y destinos serializables |
| `session` | Kotlin/JVM puro: identidad, resultado de acceso y contrato de autenticación |
| `core-platform` | BaseViewModel compartido por las features |
| `core-designsystem` | Tema Material 3, Manrope local, espaciados, componentes y formato PEN |
| `features:auth` | Formulario, validación visual, carga, errores y acceso |
| `features:home` | Resumen, gráfico interactivo semanal/mensual, movimientos y logout |

No hay dependencias entre features. La UI depende de contratos; las implementaciones se seleccionan en Koin mediante `CoreDataModule`. Dominio, navegación y sesión se compilan sin Android. Cada feature conserva `AuthGraph`/`HomeGraph` y `AuthModule`/`HomeModule` en su paquete raíz. La UI se organiza por pantalla: `auth/ui/login/LoginScreen.kt`, `auth/ui/login/LoginViewModel.kt`, `home/ui/home/HomeScreen.kt` y `home/ui/home/HomeViewModel.kt`. Los packages siguen esas carpetas; las pruebas reflejan la misma organización. Login separa `LoginUiState.kt`, `LoginUiIntent.kt` y `LoginError.kt`; Home separa `HomeUiState.kt`, `HomeUiIntent.kt` y `HomeContent.kt`. Los ViewModels contienen solo su lógica; cada contrato de nivel superior tiene su propio archivo. El ensamblaje global vive en `app/MainGraph.kt`; `MainActivity` solo configura la ventana y abre la app Compose.

## Inicialización e inyección

`AndroidApplication` es la Application registrada en el manifest. `onCreate` llama a `koinInitializer`, que ejecuta `startKoin`, registra `androidContext(this@AndroidApplication)`, habilita `androidLogger` y carga únicamente `AppModule().module`.

`AppModule` usa `@Module(includes = [CoreDataModule::class, AuthModule::class, HomeModule::class])` y `@ComponentScan`. Cada feature expone su clase `@Module`/`@ComponentScan`; sus ViewModels se registran con `@KoinViewModel`. `CoreDataModule` proporciona los repositorios compartidos mediante funciones `@Single`, vinculadas a sus interfaces. El contexto inyectado es el de Application; los ViewModels conservan su lifecycle y no se registran como singletons.

`plugin.koin` centraliza KSP, Koin Annotations y el procesador. Se aplica en app, datos y mediante `plugin.feature` en las features. El catálogo fija KSP 2.3.11 y Koin Annotations/Compiler 2.2.0. Se deshabilita el módulo predeterminado con `KOIN_DEFAULT_MODULE=false`; no se combinan registros DSL con los módulos generados. `org.koin.ksp.generated.module` se importa en el arranque y el código generado no se edita.

## MVVM con flujo MVI

`ViewModel` es el propietario del estado de cada pantalla (MVVM). Las acciones entran exclusivamente por `onIntent`, que produce un `StateFlow<UiState>` inmutable para renderizar (MVI). Las pantallas reciben estado y callbacks; las entradas de grafo resuelven ViewModels mediante Koin y observan con `collectAsStateWithLifecycle`.

- Auth valida correo y longitud de contraseña; evita envíos duplicados, conserva el correo y permite reintentar. Una cuenta demo incorrecta nunca crea sesión.
- Home distingue carga, contenido, vacío y error; admite reintento, selección de periodo y barra, confirmación ilustrativa y logout.
- Las operaciones pertenecen a `viewModelScope`; cancelación se propaga y nunca se convierte en error de negocio.
- `core-platform/BaseViewModel` centraliza el StateFlow y su actualización atómica, siguiendo AndroidBase. Las intenciones, cargas, reintentos y políticas de concurrencia siguen siendo responsabilidad de cada feature.
- No se utiliza un canal de eventos para login/logout. La sesión es estado durable en memoria; `MainGraph` observa su identidad. Los avisos y diálogos permanecen en estado hasta ser descartados, evitando perder efectos al ir a segundo plano.

## Navegación y sesión

```text
AuthGraph / Login -- sesión creada --> HomeGraph / Home
HomeGraph / Home -- cerrar sesión --> AuthGraph / Login
```

Rutas tipadas mediante Kotlin Serialization y Navigation Compose. Cada feature registra su grafo; `app` coordina el cambio de sesión. Al cambiar de identidad, `popUpTo` inclusivo elimina el historial y los ViewModels de la sesión anterior; `launchSingleTop` evita duplicados. Si se restaura el grafo correcto al recrear la Activity, no se vuelve a navegar. Home no renderiza contenido sin sesión, incluso durante restauración. Atrás desde Home sale de la Activity y no vuelve al formulario; después de logout no permite recuperar el Home anterior.

Los otros destinos del diseño muestran un aviso de próxima etapa, sin crear features adicionales. La confirmación de notificación es ilustrativa y no cambia importes.

## Diseño centralizado y apariencia

Las dos features usan el mismo `PagoControlTheme`, con paletas del `DESIGN.md`: amarillo de marca, fondo cálido claro y superficies oliva oscuras. No se habilita color dinámico para conservar la identidad. Manrope se empaqueta localmente, con pesos variables y números tabulares; su licencia está en `core-designsystem/src/main/assets/OFL-Manrope.txt`. Los colores semánticos se resuelven a través de MaterialTheme.

El menú de apariencia está disponible en Auth y Home: sistema, claro y oscuro. La preferencia es global al dispositivo, independiente de la sesión, y persiste en DataStore (`appearance`, clave `theme_mode`, valores `System`, `Light`, `Dark`). Valores ausentes/desconocidos usan el sistema. Un fallo de lectura IO ofrece el valor predeterminado sin sobrescribir el archivo; un fallo de escritura muestra un aviso. Se espera la primera lectura antes de dibujar las pantallas.

Se incluyen previews claras y oscuras, etiquetas accesibles, importes con signo, scroll, insets y adaptación al teclado. El contenido tiene ancho máximo en pantallas amplias. TalkBack, tablets y escalado extremo requieren una revisión manual adicional; no se certifica accesibilidad únicamente por previews.

## Build y stack

`buildSrc/src/main/kotlin/Config.kt` centraliza SDK 37, mínimo 24 y Java 21. Los scripts de `plugin/` proporcionan `plugin.application`, `plugin.android.lib`, `plugin.kotlin.lib` y `plugin.feature`, como en AndroidBase. `plugin.compose` extrae la configuración visual común entre features y designsystem; `plugin.feature` concentra las dependencias compartidas de las dos features. Cada módulo declara su namespace y las dependencias específicas que consume. El catálogo conserva versiones concretas: AGP 9.3.3, Gradle 9.5.0, Kotlin/Compose compiler 2.4.10, Compose BOM 2026.02.01, Navigation 2.10.0, Koin 4.2.2, coroutines 1.10.2 y DataStore 1.1.7.

Se usa Kotlin integrado de AGP en módulos Android; Kotlin JVM se aplica a dominio, sesión y navegación. Los plugins se cargan desde `buildSrc` con versiones del catálogo. Koin usa anotaciones y KSP con módulos por propietario y composición en `AppModule`. Se conserva la variante debug existente. Firebase, HTTP, sabores dev/prod y scripts de despliegue de la base no se incorporan; se mantienen las dos features y el comportamiento aprobado.

Referencias oficiales consultadas: [Kotlin integrado de AGP](https://developer.android.com/build/migrate-to-built-in-kotlin), [Koin y Compose](https://insert-koin.io/docs/quickstart/android-compose/). La compatibilidad efectiva se verifica con el wrapper y las tareas del proyecto.

## Verificación reproducible

Requiere JDK 21, Android SDK 37 y `local.properties` con `sdk.dir` local. No contiene secretos ni archivos Firebase.

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :core-domain:test :core-data:testDebugUnitTest :features:auth:testDebugUnitTest :features:home:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas locales cubren validación, sesión inválida, dashboard protegido, logout, doble envío, reintento y selección de periodo. La prueba instrumentada recorre acceso, Home, cambio a oscuro, recreación y dos sesiones consecutivas. Las pruebas usan implementaciones controladas; no acreditan un proveedor de autenticación real.

Validación inicial ejecutada el 8 de noviembre de 2026: APK debug generado; ocho pruebas unitarias aprobadas; dos pruebas instrumentadas aprobadas en Pixel 7, Android 16 (incluida la prueba de plantilla existente).

Tras migrar a `AndroidApplication` y Koin Annotations/KSP se ejecutaron nuevamente `:app:assembleDebug`, las seis pruebas unitarias de datos/Auth/Home, `:app:lintDebug` y las dos pruebas instrumentadas de `:app:connectedDebugAndroidTest`, todas correctamente. Se inspeccionó `AppModule` generado: incluye los tres módulos y registra el ViewModel de app sin duplicar las definiciones de las features. Lint finaliza con cero errores y veintidós advertencias; no se silencian checks ni se actualizan dependencias solo para eliminar avisos.

Se revisaron visualmente Auth claro y Home claro/oscuro en el emulador. Capturas locales en `app/build/screenshots/` y APK en `app/build/outputs/apk/debug/app-debug.apk`; son artefactos de build. No se ejecutó una auditoría manual completa con TalkBack ni la matriz de tablets/dispositivos antiguos.

## Próxima integración

Para autenticar cuentas reales, sustituir `DemoAuthRepository` en `CoreDataModule` por un adaptador de proveedor, implementar restauración/expiración de sesión y mapear errores al contrato. Sustituir `DemoHomeRepository` por datos de esa identidad. Configuración Firebase, registro, recuperación, permisos SMS, sincronización y los demás features quedan fuera de este hito. Los datos demo no se migran a cuentas reales.

Skills aplicadas de `docs/skills`: android-modular, project-map, bootstrap, gradle-conventions, modularization, clean-domain, feature, mvi, navigation, koin, design-system, compose-ui, auth-session, coroutines, data-repositories, datastore, accessibility, unit-testing y ui-testing. Referencia de estructura: `docs/skills/AndroidBase`.
