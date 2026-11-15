---
name: android-modular
description: Crear o ampliar aplicaciones Android con arquitectura Gradle modular al estilo de CvFree, con buildSrc, features y modulos compartidos. Usar al iniciar proyectos Android, agregar funcionalidades o modularizar una aplicacion por pedido del usuario.
---

# Android modular

La preferencia del usuario es organizar las aplicaciones Android en modulos Gradle reales, siguiendo la estructura de CvFree. No basta con crear paquetes o carpetas dentro de app.

## Estructura base

```text
app/
buildSrc/
  src/main/kotlin/
    plugin/
features/
  <funcionalidad>/
core-domain/
core-data/
core-designsystem/
core-platform/
navigation/
session/
gradle/libs.versions.toml
settings.gradle.kts
```

- `app`: punto de entrada, composicion de dependencias, configuracion global y ensamblaje del grafo principal. Ubicar las pantallas y la logica de cada funcionalidad en su feature.
- `buildSrc`: plugins de convencion y configuracion compartida de compilacion. Reutilizar convenciones para application, bibliotecas Android, bibliotecas Kotlin y features; evitar copiar bloques Gradle entre modulos.
- `features/<funcionalidad>`: modulo Android independiente por capacidad del producto, con capas `data/`, `domain/` y `ui/` propias, como Auth. Incluye sus repositorios, fuentes Firebase/Room, entidades, reglas y coordinacion. Varias pantallas del mismo flujo pueden pertenecer al mismo modulo.
- `core-domain`: solo contratos, modelos y eventos globales usados por varias capacidades, sin dependencias de Android, UI ni implementaciones de datos. El dominio exclusivo de Home, Auth u otra feature pertenece a `features/<nombre>/domain/`.
- `core-data`: implementaciones y persistencia de capacidades globales compartidas. Los repositorios, fuentes remotas/locales, bases Room y mapeadores de una feature pertenecen a su propio `data/`, no a este modulo.
- `core-designsystem`: tema, componentes visuales y recursos compartidos.
- `core-platform`: integraciones y utilidades Android compartidas que no pertenecen a una funcionalidad concreta.
- `navigation`: rutas y contratos compartidos. El ensamblaje de destinos concretos puede vivir en app para evitar dependencias circulares.
- `session`: modelos y contratos compartidos de sesion cuando exista autenticacion o estado de usuario transversal.

En proyectos nuevos, usar esta base y crear las features necesarias. Crear session y los demas modulos compartidos con responsabilidades concretas a medida que se necesiten; evitar modulos vacios o uno por cada pantalla. Respetar una estructura alternativa cuando el usuario la solicite expresamente.

## Organizacion interna de features

Esta biblioteca usa paquetes por pantalla. Conserva `<Feature>Graph.kt` y `<Feature>Module.kt` en el paquete raiz de la feature; ubica la UI en `ui/<pantalla>/`. Por ejemplo:

```text
features/auth/.../features/auth/
  AuthGraph.kt
  AuthModule.kt
  ui/login/
    LoginScreen.kt
    LoginViewModel.kt
    LoginUiState.kt
    LoginUiIntent.kt
    model/
      LoginError.kt
features/home/.../features/home/
  HomeGraph.kt
  HomeModule.kt
  ui/home/
    HomeScreen.kt
    HomeViewModel.kt
    HomeUiState.kt
    HomeUiIntent.kt
    components/
      HomeGuideOverlay.kt
      model/
        HomeGuideAction.kt
    model/
      HomeContent.kt
      HomeSection.kt
    util/
      HomeChartSelection.kt
    preview/
      HomeScreenPreview.kt
```

Usa el nombre de la pantalla para sus tipos: `LoginUiState`, `LoginUiIntent` y `LoginError`, aunque la feature se llame auth. El nombre del archivo coincide con el tipo principal. Cada data class, sealed class/interface o enum de nivel superior tiene su propio archivo; no declares los contratos dentro del archivo del ViewModel. Las variantes anidadas permanecen en su contrato sealed. Crea tipos de error o contenido solo cuando la pantalla los necesite; el arbol ilustra las responsabilidades, no exige archivos vacios.

Refleja los paquetes de UI en las pruebas y actualiza imports, grafos, previews y DI al renombrar. Los paquetes Kotlin deben coincidir con las carpetas. Consulta el proyecto AndroidBase que acompana a esta biblioteca para el ejemplo portable.

Auth agrupa Login, Registro y recuperacion en `ui/login/`, `ui/register/` y `ui/forgotpassword/`, con tipos `Login*`, `Register*` y `ForgotPassword*`. No se crea un modulo Gradle por formulario. Si el producto requiere Splash y Onboarding, son features independientes con grafos y modulos Koin propios; la preferencia de finalizacion se define en dominio y se persiste en datos, sin dependencias entre features.

## Dependencias y configuracion

- Mantener el flujo principal `app -> features -> core-domain` y `core-data -> core-domain`. Conectar contratos e implementaciones desde la composicion de dependencias de la aplicacion.
- Las features pueden depender de designsystem, platform, navigation y session cuando los necesiten. Evitar acoplarlas directamente entre si; exponer contratos o rutas compartidas para la comunicacion.
- Mantener dominio independiente de datos y presentacion. Evitar que los modulos compartidos dependan de features o de app y no introducir ciclos.
- Registrar cada modulo en `settings.gradle.kts`, por ejemplo `include(":features:auth")`. buildSrc es el build especial de Gradle y no se registra como modulo de la aplicacion.
- Cada modulo de producto debe tener su `build.gradle.kts`, plugin adecuado, namespace cuando corresponda y dependencias minimas. Crear manifiestos y recursos solo donde se necesiten.
- Centralizar versiones y alias de librerias en `gradle/libs.versions.toml`; mantener convenciones y configuracion comun de compilacion en buildSrc. No fijar versiones de SDK, JDK o librerias copiadas de CvFree.

## Aplicacion a proyectos existentes

Leer las instrucciones del repositorio, settings, catalogo y convenciones antes de editar. Preservar nombres, inyeccion de dependencias y patrones de UI establecidos. Agregar una nueva funcionalidad al modulo que le corresponde o crear su modulo cuando sea una capacidad independiente; extraer lo compartido cuando tenga consumidores reales.

Una tarea puntual no autoriza una migracion completa. Si se solicita modularizar, mover tambien dependencias, recursos y registros necesarios para conservar el comportamiento, no solo los archivos Kotlin. No imponer Firebase, Koin u otras integraciones por aparecer en el proyecto de referencia.

Validar los modulos afectados con el wrapper de Gradle y las variantes existentes; ejecutar compilacion y pruebas pertinentes al cambio. Informar bloqueos de SDK o configuracion externa sin introducir secretos ni cambiar versiones ajenas a la tarea.

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

## Imports Kotlin

Declara las referencias a clases, funciones, anotaciones y propiedades externas mediante imports explicitos al inicio del archivo, despues de package. Usa nombres simples en el cuerpo del codigo, incluidas pantallas, previews y pruebas; evita llamadas con paquetes completos como `com.devpaul.pagocontrol.designsystem.PagoHeader(...)` o `androidx.compose.foundation.layout.PaddingValues(...)`. Ante nombres en conflicto, usa un alias de import descriptivo (`import otro.paquete.R as FeatureR`). Ordena los imports, evita duplicados y no uses comodines. Conserva los nombres de paquete que sean datos en strings, SQL, manifiestos y configuracion Gradle. Al revisar esta regla, abarca todos los modulos de producto y sus pruebas, excluyendo fuentes generadas y plantillas de referencia salvo pedido explicito.

## Formato de Android Studio

La preferencia del usuario es el formato de `Reformat Code` (`Ctrl + Alt + L`) de Android Studio. Al crear o modificar codigo, aplica el formateador del IDE con el esquema del proyecto; respeta `.idea/codeStyles/Project.xml` y `.idea/codeStyles/codeStyleConfig.xml` cuando existan. Esto incluye pantallas como `RegisterScreen.kt`, componentes, previews, pruebas y scripts Gradle Kotlin, asi como XML y otros formatos compatibles. Deja al formateador decidir sangria, espacios, saltos de linea y distribucion de argumentos; no comprimas manualmente llamadas o declaraciones largas despues de formatear.

Para ejecucion por consola, usa `bin/format.bat` (Windows) o el equivalente del IDE con `-s <esquema.xml>` y rutas explicitas del codigo de producto. Si Android Studio esta abierto y bloquea otra instancia, usa directorios temporales separados de configuracion y sistema para el formateador, sin cerrar el IDE del usuario. Evita sustituirlo por otro formateador con reglas distintas. Conserva las reglas de imports y argumentos con nombre: el formato no las reemplaza. Excluye archivos generados, builds, dependencias y `docs/skills/AndroidBase` salvo pedido explicito. En tareas de documentacion sobre esta preferencia, actualiza solo los skills pertinentes de `docs/skills`.

## Separacion de pantallas y componentes

En el nivel directo de `ui/<pantalla>/`, conserva solo `<Pantalla>Screen.kt`, `<Pantalla>ViewModel.kt`, `<Pantalla>UiState.kt`, `<Pantalla>UiIntent.kt` y, si el flujo los necesita, `<Pantalla>Effect.kt` o `<Pantalla>UiEvent.kt`. La pantalla ensambla los componentes; extrae tarjetas, filas, overlays y controles visuales a `components/`, con un archivo por componente principal y nombre coincidente. Usa la visibilidad predeterminada para los composables consumidos desde otros archivos; no les agregues `internal` por rutina. Las previews y detalles privados pueden conservar `private`.

Los modelos auxiliares de estado o contenido van en `model/`; los modelos exclusivos de un componente, en `components/model/`. Separa cada data class o enum en su archivo, sin mezclarlo con composables. Ubica utilidades en `util/` y previews en `preview/` cuando ayuden a mantener limpia la pantalla. No crees carpetas, estados, ViewModels o eventos vacios. Una pantalla secundaria completa usa su propio paquete `ui/<pantalla>/`, aunque reciba estado y callbacks del flujo padre.

Al mover archivos, actualiza package, imports, previews, consumidores y pruebas; refleja los paquetes de utilidades en sus pruebas. En Home, `HomeGuideAction` esta separado de `HomeGuideOverlay`, los componentes viven en `ui/home/components/` y la revision en `ui/notificationreview/`. No cambies AndroidBase al documentar o aplicar esta preferencia al producto.

## Capas propias de cada feature

La referencia para organizar Home es Auth: `features/home/.../home/data/` contiene `datasource/local/`, `datasource/remote/`, `repository/`, `mapper/` y `sync/`; `domain/` contiene `entity/`, `repository/`, validaciones y reglas de reconocimiento; `ui/` conserva las pantallas y componentes. No coloques el dominio o la persistencia de Home en core por usar Firebase, Room o servicios Android. Los SDK y DTO quedan en data; domain no importa Firebase, Room, Context ni Compose. Usa casos de uso cuando aporten reglas u orquestacion, sin crear delegaciones vacias.

Registra los proveedores y componentes en el `<Feature>Module`, declara sus dependencias Firebase/Room en el Gradle de la feature y mueve tambien pruebas, manifiestos de pruebas y esquemas Room. Conserva el nombre del archivo de base de datos, las migraciones y los esquemas historicos al cambiar paquetes; el directorio de exportacion usa el nombre cualificado completo de la clase Database. La composicion de app puede conectar los contratos publicos de Home con servicios globales, sin crear dependencias de core hacia la feature ni dependencias directas entre features. Reserva core para capacidades realmente compartidas.

## Dependencias centralizadas con DependenceUtil

Declara las librerias externas y sus configuraciones de consumo en `buildSrc/src/main/kotlin/dependence/DependenceUtil.kt`, mediante funciones de extension `Project` como `composeImplementation()`, `firestoreImplementation()` o `roomImplementation()`. Resuelve alias desde el catalogo `libs.versions.toml`; no dupliques coordenadas ni versiones. Los build.gradle.kts de producto llaman funciones o aplican convenciones, sin bloques implementation(libs...) repetidos.

Los plugins `plugin.feature`, `plugin.application`, `plugin.compose`, `plugin.koin` y `plugin.kotlin.lib` llaman las funciones comunes a sus consumidores. Las features reciben Compose, navegacion, Koin y pruebas desde sus convenciones. Firebase, Room o DataStore se agregan con funciones solo a quienes los usan. Conserva las dependencias project(...) visibles en los modulos o su convencion compartida y la configuracion particular (namespace, buildTypes, exportacion de esquemas) en el propietario. Los artefactos Gradle necesarios para compilar buildSrc permanecen en buildSrc/build.gradle.kts: ese build no puede usar funciones que todavia esta compilando. AndroidBase es referencia y no se modifica.

Al refactorizar, conserva los scopes implementation, ksp, debugImplementation y androidTestImplementation, los BOM y el grafo entre modulos. Evita que dos convenciones declaren el mismo grupo en un consumidor. Valida compilacion debug, compilacion de pruebas instrumentadas, unit tests y una variante release para comprobar los scopes.
