# Core Design System

Lenguaje visual y componentes Compose reutilizables de CvFree.

## Qué va aquí

- Colores, tipografía, formas, espaciados y tema Material.
- Componentes visuales compartidos, como botones, campos y diálogos.
- Iconos y recursos visuales comunes.
- Estados visuales genéricos, como carga, error y contenido vacío.
- Previews de los componentes del design system.

## Qué no va aquí

- Pantallas completas de una feature.
- ViewModels o estado de negocio.
- Navegación, repositorios o acceso a datos.
- Componentes que solamente tengan sentido dentro de una feature.

## Regla de dependencia

Puede depender de Compose y librerías estrictamente visuales. No debe depender de `core-data`, `session`, `navigation` ni de features.
