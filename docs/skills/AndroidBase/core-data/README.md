# Core Data

Capa de infraestructura y acceso a datos de la aplicación.

## Qué va aquí

- Implementaciones de los repositorios definidos en `core-domain`.
- Servicios Retrofit y configuración de red.
- Fuentes de datos remotas y locales.
- Integraciones con Firebase.
- Persistencia con DataStore o base de datos.
- DTO, entidades locales y mapeadores hacia modelos de dominio.
- Implementaciones de los contratos definidos en `session`.
- Módulos Koin relacionados con infraestructura.

## Qué no va aquí

- Composables, pantallas o ViewModels.
- Reglas de negocio y casos de uso.
- Rutas o decisiones de navegación.
- Modelos de dominio dependientes de Retrofit, Firebase o Android.

## Dependencias permitidas

Puede depender de `core-domain` y `session`. Ninguno de esos módulos debe depender de `core-data`.
