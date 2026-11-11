# AndroidBase: referencia de arquitectura

Esta carpeta conserva la referencia modular de CvFree proporcionada para orientar la implementación de PagoControl. No forma parte del build principal ni representa funcionalidades ya habilitadas en PagoControl.

## Contenido

- `app`: arranque, composición de dependencias, navegación principal y componentes globales de UI.
- `features/auth`: autenticación Firebase organizada en datos, dominio y presentación.
- `features`: ejemplos de splash, onboarding, inicio, currículums y ajustes.
- `core-domain`, `core-data`, `core-designsystem`, `core-platform`, `navigation` y `session`: responsabilidades compartidas.
- `firebase`: ejemplos de reglas, funciones y pruebas del backend de la referencia.

Los README de cada módulo describen sus responsabilidades. Las integraciones deben adaptarse al dominio, los módulos y el estilo visual de PagoControl; no se incorporan automáticamente planes, facturación, currículums ni otros flujos de CvFree.

## Configuración local

Se excluyen configuraciones locales de Firebase, claves de firma, dependencias descargadas y artefactos compilados. No se incluye el APK de la referencia.

La implementación vigente de PagoControl y su configuración Firebase se documentan en [ARCHITECTURE.md](../../ARCHITECTURE.md) y [FIREBASE_SETUP.md](../../FIREBASE_SETUP.md).
