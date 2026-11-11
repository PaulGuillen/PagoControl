# App

Módulo de aplicación y punto de composición de CvFree.

## Qué va aquí

- `AndroidApplication` y la inicialización global de Koin y Timber.
- `MainActivity` y el `NavHost` principal.
- Inclusión de los módulos Koin de datos, navegación y features.
- Configuración global de la aplicación y flujos que afectan a toda la app.
- Integraciones exclusivas del proceso de aplicación, como actualización obligatoria.

## Qué no va aquí

- Reglas de negocio, casos de uso o entidades.
- Implementaciones de repositorios, APIs o almacenamiento local.
- Pantallas que pertenezcan a una feature concreta.
- Componentes visuales reutilizables del design system.

## Dependencias permitidas

`app` puede depender de todos los módulos porque es el punto donde se ensambla la aplicación. Los demás módulos no deben depender de `app`.
