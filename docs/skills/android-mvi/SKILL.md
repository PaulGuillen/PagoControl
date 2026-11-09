---
name: android-mvi
description: Implementar UiState, UiIntent, UiEvent y ViewModels con flujo unidireccional en Compose. Usar para formularios, pantallas y coordinación de efectos de presentación.
---

# Modelar estado y eventos

## Nombres y archivos

Nombra cada contrato por pantalla, no por la feature contenedora: `LoginViewModel`, `LoginUiState`, `LoginUiIntent` y, si corresponde, `LoginError`. Para Home usa `HomeViewModel`, `HomeUiState` y `HomeUiIntent`. Ubicalos en `ui/login/` o `ui/home/`, con packages coincidentes.

Cada tipo de nivel superior tiene un archivo con su mismo nombre: estados, intenciones sealed, errores enum y contenidos sealed se separan del ViewModel. El archivo del ViewModel contiene su clase y logica, sin contratos adicionales. Las variantes anidadas del sealed se mantienen juntas dentro de su contrato. No crees contratos sin uso.

## Procedimiento

1. Define `UiState` inmutable con lo necesario para dibujar la pantalla. Modela carga, datos, validación y error sin combinaciones contradictorias.
2. Modela acciones como `UiIntent` y actualiza estado desde el ViewModel con transformaciones atómicas cuando haya productores concurrentes.
3. Ejecuta casos de uso en `viewModelScope`. Impide doble envío o define explícitamente si una acción sustituye, encola o ignora la anterior.
4. Separa estado durable de efectos transitorios. Define si un evento puede perderse mientras la UI está detenida y si necesita confirmación de consumo. Un Channel o SharedFlow no garantiza por sí solo entrega exactamente una vez.
5. Mantén errores de campos en estado y efectos como snackbar/navegación bajo una política de recolección de lifecycle. Usa `SavedStateHandle` para claves pequeñas recuperables; persiste borradores importantes.
6. Reutiliza una clase base solo si reduce duplicación sin ocultar concurrencia, cancelación y entrega de eventos.

## Comprobar y entregar

Prueba transiciones, doble toque, reintento y vuelta desde segundo plano. La recomposición no debe repetir navegación ni iniciar solicitudes. Entrega contrato del estado y de los efectos.
