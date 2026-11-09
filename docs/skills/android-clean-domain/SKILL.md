---
name: android-clean-domain
description: Modelar entidades, contratos, casos de uso y errores de negocio en Kotlin independiente de Android. Usar al definir o corregir la capa de dominio.
---

# Implementar dominio limpio

## Procedimiento

1. Traduce acciones del producto a reglas verificables: entradas, invariantes, resultados y fallos esperables.
2. Define modelos de dominio y contratos de repositorio sin DTO, Firebase, Context, Compose, recursos Android ni anotaciones Koin.
3. Usa funciones `suspend` para operaciones y `Flow` para observación cuando la semántica lo requiera. Explicita nulabilidad, orden y consistencia.
4. Añade casos de uso cuando concentren validaciones, autorización de negocio u orquestación reutilizable. Evita capas que solo renombren llamadas sin aportar una frontera útil.
5. Representa fallos esperados con tipos propios. Mantén las excepciones técnicas en infraestructura y el texto localizado en presentación.
6. Inyecta reloj y generador de IDs cuando determinen reglas verificables. Propaga cancelación; nunca conviertas `CancellationException` en un error de negocio.

## Comprobar y entregar

Entrega contratos y casos de uso con pruebas de invariantes, límites y resultados negativos. El módulo debe compilar y probarse sin SDK Android ni servicios remotos. Documenta garantías que dependen del servidor, como cuotas y unicidad global.
