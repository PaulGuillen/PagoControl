# Core Domain

Capa central de reglas de negocio de CvFree.

## Qué va aquí

- Entidades y modelos de dominio.
- Interfaces de repositorios.
- Casos de uso.
- Validaciones y reglas de negocio reutilizables.
- Errores y resultados propios del dominio.
- Interfaces de servicios que el dominio necesita, sin detalles de implementación.

## Qué no va aquí

- Clases de Android o Compose.
- Koin, Retrofit, Firebase, DataStore o bases de datos.
- DTO, respuestas de API o entidades de persistencia.
- ViewModels, pantallas o navegación.

## Regla de dependencia

Debe ser Kotlin puro y no depender de capas externas. Las demás capas dependen de sus contratos, nunca al contrario.
