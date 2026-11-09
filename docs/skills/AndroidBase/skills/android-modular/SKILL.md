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
- `features/<funcionalidad>`: modulo Android independiente por capacidad del producto, con pantallas, estado de UI, ViewModels y coordinacion propios. Varias pantallas del mismo flujo pueden pertenecer al mismo modulo.
- `core-domain`: modelos de negocio, casos de uso y contratos de repositorios, sin dependencias de Android, UI ni implementaciones de datos.
- `core-data`: implementaciones de repositorios, fuentes remotas/locales, persistencia y mapeadores; depende de los contratos de dominio.
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
    LoginError.kt
features/home/.../features/home/
  HomeGraph.kt
  HomeModule.kt
  ui/home/
    HomeScreen.kt
    HomeViewModel.kt
    HomeUiState.kt
    HomeUiIntent.kt
    HomeContent.kt
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
