---
name: android-unit-testing
description: Escribir pruebas unitarias de reglas, ViewModels, mapeos y repositorios Kotlin Android. Usar para validar comportamiento crítico y reproducir errores.
---

# Probar lógica local

## Procedimiento

1. Selecciona invariantes con riesgo real: límites, transiciones, pérdida de datos, autorización local de presentación y concurrencia. Evita pruebas que solo repliquen implementación.
2. Mantén dominio en JVM sin Android. Sustituye repositorios, reloj y servicios por fakes controlados cuando permitan observar comportamiento.
3. Para coroutines, usa `runTest`, scheduler compartido y dispatcher Main de prueba cuando lo requiera el ViewModel. Evita sleeps reales.
4. Verifica estados/resultados y efectos según su contrato. Incluye cancelación, doble solicitud y respuesta fuera de orden cuando importen.
5. En repositorios prueba mapeos corruptos, errores y consistencia; no presentes un fake en memoria como prueba de reglas Firestore desplegadas.
6. Reproduce un bug con un caso que falle antes del cambio cuando sea viable y ejecuta primero el módulo afectado.

## Comprobar y entregar

Ejecuta tareas existentes, por ejemplo `:core-domain:test` o una tarea `test<Variante>UnitTest` confirmada. Reporta comandos, resultado y dependencias externas no cubiertas; amplía el alcance solo si los cambios lo justifican.
