---
name: android-feature
description: Implementar una funcionalidad Android completa conectando dominio, datos, estado, UI, inyección y navegación. Usar para agregar un flujo de producto nuevo.
---

# Construir una feature vertical

## Procedimiento

1. Define escenarios de éxito, carga, vacío, validación, fallo y reintento. Acuerda qué datos necesita y quién es su propietario.
2. Crea `features/<nombre>` y regístralo en settings. Usa la convención Compose e incorpora solo dependencias consumidas. Conserva `<Feature>Graph.kt` y `<Feature>Module.kt` en el paquete raíz; organiza cada pantalla en `ui/<pantalla>/`, con packages que coincidan con las carpetas.
3. Define contrato/caso de uso; implementa infraestructura fuera de la UI. Si existen capas dentro de la feature, conserva la dirección de dependencias y evalúa aislamiento Gradle cuando aporte valor.
4. Implementa estado, intenciones y efectos con ViewModel. Nombra tipos y archivos por pantalla: `LoginScreen.kt`, `LoginViewModel.kt`, `LoginUiState.kt`, `LoginUiIntent.kt` y `LoginError.kt` cuando corresponda; no uses el prefijo Auth para contratos exclusivos de Login. Cada tipo de nivel superior tiene su propio archivo, fuera del ViewModel; las variantes anidadas permanecen en el sealed. Separa entrada de pantalla que conecta dependencias del composable que recibe valores/callbacks y refleja la organización en las pruebas.
5. Expone una función `NavGraphBuilder.<nombre>Graph(...)`; recibe callbacks para salir del flujo. Registra rutas compartidas, DI y el grafo en la composición de `app`.
6. Añade recursos localizados, estados visuales, accesibilidad y eventos de observabilidad con valores de catálogo.

## Entregables y cierre

Entrega un flujo navegable y sus pruebas de comportamiento crítico. Comprueba compilación del módulo y de la app consumidora. Informa cambios de contratos y configuración requerida; no consideres completada una feature que solo tiene carpetas o previews.
