---
name: android-navigation
description: Implementar navegación tipada con Navigation Compose, grafos por feature y restauración de back stack. Usar al conectar pantallas, sesión y enlaces de entrada.
---

# Componer grafos y rutas

## Procedimiento

1. Define contratos serializables de grafos y destinos en `navigation`. Usa IDs y parámetros pequeños; evita documentos completos, credenciales y objetos de SDK.
2. Cada feature implementa su grafo. `app/MainGraph` registra grafos y conecta callbacks entre funcionalidades.
3. Recupera argumentos mediante las APIs tipadas compatibles, como `toRoute`. Recarga los datos desde repositorios usando el identificador.
4. Define destinos iniciales, back, `popUpTo`, `launchSingleTop` y restauración para cada flujo. Tras login retira el flujo de acceso del historial.
5. Al cerrar sesión, limpia datos compartidos y estados guardados sensibles antes de permitir otra cuenta. Restaurar una pestaña no debe recuperar contenido del usuario anterior.
6. Asigna scope al ViewModel de un flujo compartido. Mantén autosave y reglas fuera del registro de rutas.
7. Valida entradas externas, espera sesión cuando sea necesaria y consume cada solicitud de navegación con una política explícita.

## Comprobar y entregar

Verifica login → inicio → detalle → atrás, cambio de pestaña, logout, recreación y entrada desde notificación. Entrega mapa de grafos y política de restauración. No migres de familia de navegación sin que el proyecto lo requiera.

## Convencion de rutas de esta base

Centraliza las rutas en `navigation/route/`: los contratos de nivel superior `AppRoute`, `GraphRoute : AppRoute` y `DestinationRoute : AppRoute`, en ese orden y juntos en `AppRoute.kt`. Cada feature tiene una interfaz sellada propia: `AuthRoute`, `HomeRoute`, `SplashRoute` y `OnboardingRoute`. Dentro declara `@Serializable data object Graph` con `GraphRoute`, y destinos con `DestinationRoute`: `AuthRoute.Login/Register/ForgotPassword`, `HomeRoute.Home`, `SplashRoute.Splash` y `OnboardingRoute.Welcome`. Los objetos anidados permanecen en el archivo de su contrato sellado.

Cada feature consume su contrato con `navigation<AuthRoute.Graph>(startDestination = AuthRoute.Login)` y `composable<AuthRoute.Login>`; no conserva rutas planas duplicadas ni importa otras features. `MainGraph` compone los grafos y usa estas mismas rutas para navegar, comprobar jerarquias y limpiar el historial. No agregues destinos de referencia sin pantallas implementadas.

`NavigationModule` usa `@Module` y `@ComponentScan`, incluido desde `AppModule`; las rutas serializables no se registran como singletons. El modulo sigue siendo Kotlin/JVM, sin NavController global ni dependencia de Android.

## Argumentos de llamadas

Las llamadas Kotlin con tres o mas argumentos suministrados usan nombres explicitos en todos los argumentos entre parentesis, incluidos constructores y callbacks de pantallas: `LoginScreen(state = state, onIntent = viewModel::onIntent, ...)`. Las llamadas largas de pantallas se distribuyen con un argumento por linea. La lambda final idiomatica de Compose puede quedar fuera de los parentesis y cuenta para el umbral. Conserva varargs posicionales y no fuerces argumentos con nombre en APIs Java o valores de tipo funcion.

## Salida de la introduccion

En esta base, omitir, entrar y completar los tres pasos del onboarding persisten la finalizacion y abren exclusivamente `AuthRoute.Graph` con `AuthRoute.Login` como destino inicial. El callback `onFinished` no recibe un selector de registro. Registro se abre desde Login, no al terminar la introduccion. Conserva el bloqueo de doble envio y el reintento si falla DataStore.
