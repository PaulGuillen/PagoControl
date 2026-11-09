---
name: android-compose-ui
description: Construir pantallas Jetpack Compose con estado elevado, lifecycle y efectos controlados. Usar al implementar o corregir interfaces Android.
---

# Construir pantallas Compose

## Ubicacion y nombres

Coloca cada pantalla en `ui/<pantalla>/`, con archivo y composable `<Pantalla>Screen`. Usa el mismo prefijo para su ViewModel y contratos: por ejemplo `LoginViewModel`, `LoginUiState` y `LoginUiIntent`. Los contratos de estado, intenciones y errores tienen archivos propios; no se declaran dentro del archivo de pantalla ni del ViewModel. Actualiza previews, recursos R e imports de navegacion al mover paquetes.

## Procedimiento

1. Separa la entrada que obtiene ViewModel/estado de la pantalla que recibe valores y callbacks. Usa `collectAsStateWithLifecycle` para observación Android cuando corresponda.
2. Modela contenido, carga, vacío, error y reintento. Mantén formularios utilizables con teclado, scroll e insets del sistema.
3. Usa `remember` para estado efímero y `rememberSaveable` para valores pequeños recuperables. La información de negocio importante necesita propietario y persistencia explícitos.
4. Ejecuta efectos con claves estables y propósito claro; retira observers mediante `DisposableEffect`. No hagas red, guardados ni navegación en el cuerpo de un composable.
5. Usa listas lazy con claves estables y componentes acotados. Calcula estado derivado cuando reduzca trabajo o invalide correctamente; no agregues optimizaciones sin evidencia.
6. Soporta anchos disponibles y orientación sin dimensiones rígidas que corten contenido. Ofrece previews con datos controlados y sin SDK remoto.

## Comprobar y entregar

Verifica rotación, back, teclado, recomposición y recuperación de errores. La pantalla debe poder renderizarse y probarse con estado/callbacks sin iniciar servicios reales.
