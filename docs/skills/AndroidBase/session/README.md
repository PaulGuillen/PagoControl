# Session

Contratos y modelos que representan la sesión activa del usuario.

## Qué va aquí

- Estado de sesión, por ejemplo autenticada, anónima o expirada.
- Modelos mínimos relacionados con la identidad de la sesión.
- Interfaces para consultar, observar o limpiar la sesión.
- Tipos Kotlin puros que puedan probarse sin Android.

## Qué no va aquí

- DataStore, SharedPreferences o almacenamiento Android.
- Firebase Auth, Retrofit o detalles de tokens remotos.
- Pantallas, ViewModels o navegación.
- Reglas de negocio propias de autenticación.

## Regla de dependencia

Debe mantenerse como módulo Kotlin puro. Las implementaciones de sus contratos pertenecen a `core-data`.
