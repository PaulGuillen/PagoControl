# Navigation

Contratos compartidos para la navegación tipada con Compose.

## Estructura

- `route/AppRoute.kt`: contratos base para grafos y destinos.
- `route/*Route.kt`: rutas agrupadas por flujo, por ejemplo `AuthRoute`.
- Cada feature implementa su propio `NavGraphBuilder` y consume estas rutas.

## Contratos base

- `AppRoute` mantiene un catálogo cerrado y centralizado en este módulo.
- `GraphRoute` distingue los contenedores `navigation<T>()` para poder administrar un
  flujo completo en el back stack.
- `DestinationRoute` distingue las pantallas `composable<T>()` y admite destinos con o
  sin argumentos.

Son interfaces porque una ruta puede pertenecer simultáneamente a su flujo y a una
categoría de navegación. Una jerarquía de clases obligaría a elegir una sola herencia.

## Catálogo actual

- `AuthRoute`: `Graph`, `Splash`.
- `HomeRoute`: `Graph`, `Home`.
- `OnboardingRoute`: `Graph`, `Welcome`.
- `ResumeRoute`: `Graph`, `ResumeList`.
- `SettingsRoute`: `Graph`, `Settings`.

Solo `AuthRoute` está registrado actualmente. Las demás rutas son contratos preparados
para conectarse cuando sus features implementen pantallas y grafos.

## Rutas parametrizadas

Los argumentos forman parte de una ruta serializable. No se pasan `Bundle` ni cadenas
construidas manualmente.

```kotlin
@Serializable
data class Detail(
    val itemId: String,
) : DestinationRoute
```

El destino recupera los argumentos con `SavedStateHandle.toRoute<Detail>()` o
`NavBackStackEntry.toRoute<Detail>()`.

No existe una interfaz `ParameterizedRoute`: no aportaría validación adicional. La
presencia y el tipo de los parámetros ya quedan garantizados por la `data class` y su
serializador generado.

## Reglas de dependencia

- `navigation` no depende de `core-data`, `session` ni de módulos de features.
- Las rutas y sus argumentos usan tipos simples o identificadores.
- Las pantallas Compose, ViewModels y decisiones de negocio permanecen en su feature.
- Los grafos de una feature permanecen junto a su UI para evitar dependencias circulares.
