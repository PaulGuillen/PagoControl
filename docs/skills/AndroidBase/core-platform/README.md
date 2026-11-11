# Core Platform

Adaptadores y utilidades que dependen directamente de Android.

## Qué va aquí

- Configuración de logging, como `TimberFactory`.
- Wrappers para APIs del sistema Android.
- Utilidades de conectividad, permisos, archivos o recursos del dispositivo.
- Abstracciones de plataforma que faciliten pruebas en otras capas.
- Implementaciones Android de utilidades técnicas compartidas.

## Qué no va aquí

- Tema, colores o componentes Compose; pertenecen a `core-designsystem`.
- Repositorios, Retrofit, Firebase o persistencia; pertenecen a `core-data`.
- Casos de uso o entidades; pertenecen a `core-domain`.
- Pantallas, ViewModels o rutas de navegación.

## Regla de dependencia

Debe mantenerse pequeño y enfocado en Android. No debe depender de `navigation`, `core-data` ni de módulos de features.
