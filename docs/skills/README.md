# Skills para tu próximo proyecto Android

Biblioteca portable en español basada en el código de CvFree: Kotlin, Jetpack Compose, Material 3, Gradle Kotlin DSL, Koin, coroutines/Flow y Firebase. Cada carpeta contiene una skill independiente con `SKILL.md`, descripción de activación, procedimiento y criterios para terminar.

## Cómo llevarlas al nuevo proyecto

1. Copia **el contenido de esta carpeta completo**, conservando cada subcarpeta y sus referencias.
2. Puedes guardarlo en `skills/` como documentación e indicar al agente qué `SKILL.md` leer. Una carpeta llamada `skills/` por sí sola no garantiza descubrimiento automático en cualquier herramienta.
3. Para usar descubrimiento automático, consulta la ubicación admitida por tu herramienta. Si el agente no detecta la biblioteca, proporciona la ruta del archivo explícitamente.
4. Invoca una skill por su nombre cuando tu herramienta lo permita, por ejemplo `$android-bootstrap`. Si no, pide: «Lee skills/android-bootstrap/SKILL.md y aplica sus instrucciones».
5. Sustituye nombre, package, dominio de negocio, proyectos Firebase, ambientes y proveedores por los de tu aplicación. No necesitas reproducir las funcionalidades de currículums.

Las skills son instrucciones para ejecutar trabajo, no un generador automático ni un proyecto ya configurado. Usa solo las que correspondan a la tarea; no cargues toda la biblioteca a la vez. Cada una funciona sin tener el repositorio original. La fotografía de CvFree se incluye dentro de la skill de mapeo.

## Convencion de esta biblioteca

Organiza las features por pantalla en `ui/<pantalla>/`. Usa nombres consistentes como `LoginScreen`, `LoginViewModel`, `LoginUiState`, `LoginUiIntent` y `LoginError`. Cada contrato de nivel superior vive en su propio archivo; el ViewModel contiene su logica. Las variantes anidadas del sealed permanecen en ese contrato. Consulta [android-modular](android-modular/SKILL.md) para la estructura y [android-mvi](android-mvi/SKILL.md) para los contratos.

## Orden sugerido

| Etapa | Skills |
| --- | --- |
| 1. Definir alcance | `android-project-map` |
| 2. Preparar base | `android-bootstrap`, `android-gradle-conventions` |
| 3. Definir arquitectura | `android-modularization`, `android-clean-domain` |
| 4. Construir un flujo completo | `android-feature`, `android-mvi`, `android-koin`, `android-navigation` |
| 5. Crear experiencia visual | `android-design-system`, `android-compose-ui`, `android-accessibility`, `android-localization` |
| 6. Integrar datos | `android-data-repositories` y las skills de persistencia/red necesarias |
| 7. Incorporar servicios | Autenticación, Firebase y capacidades opcionales según producto |
| 8. Verificar | Skills de pruebas, rendimiento y seguridad |
| 9. Entregar | `android-ci`, `android-release`, `android-backend-deploy` |
| 10. Mantener | `android-maintenance`, observabilidad y configuración remota |

## Catálogo y ejemplos de solicitudes

| Skill | Responsabilidad | Ejemplo de solicitud |
| --- | --- | --- |
| [android-project-map](android-project-map/SKILL.md) | Alcance, decisiones y hoja de ruta | «Mapea mi app de inventario usando este stack» |
| [android-modular](android-modular/SKILL.md) | Estructura Gradle y organización por pantalla | «Organiza las features con contratos en archivos propios» |
| [android-bootstrap](android-bootstrap/SKILL.md) | Configuración inicial y primer build | «Crea la base ejecutable del proyecto» |
| [android-gradle-conventions](android-gradle-conventions/SKILL.md) | Catálogo, plugins y variantes | «Centraliza la configuración Gradle» |
| [android-modularization](android-modularization/SKILL.md) | Módulos y límites de dependencia | «Define los módulos sin ciclos» |
| [android-clean-domain](android-clean-domain/SKILL.md) | Entidades, contratos y casos de uso | «Modela las reglas de inventario en Kotlin puro» |
| [android-feature](android-feature/SKILL.md) | Implementación vertical de una funcionalidad | «Agrega el flujo de productos de punta a punta» |
| [android-mvi](android-mvi/SKILL.md) | Estado, intenciones y efectos | «Implementa el estado del formulario» |
| [android-coroutines](android-coroutines/SKILL.md) | Concurrencia, Flow y cancelación | «Evita resultados obsoletos en la búsqueda» |
| [android-koin](android-koin/SKILL.md) | Inyección, KSP y scopes | «Registra repositorios y ViewModels» |
| [android-navigation](android-navigation/SKILL.md) | Rutas tipadas, grafos y sesión | «Conecta login, inicio y detalle» |
| [android-design-system](android-design-system/SKILL.md) | Tokens, tema y componentes | «Define el lenguaje visual reutilizable» |
| [android-compose-ui](android-compose-ui/SKILL.md) | Pantallas y ciclo de vida | «Construye listado y formulario Compose» |
| [android-accessibility](android-accessibility/SKILL.md) | TalkBack, escalado y tamaños | «Haz accesible este flujo» |
| [android-localization](android-localization/SKILL.md) | Idiomas y preferencias visuales | «Agrega español, inglés y modo oscuro» |
| [android-data-repositories](android-data-repositories/SKILL.md) | Repositorios, DTO y errores | «Implementa este contrato de datos» |
| [android-network](android-network/SKILL.md) | Retrofit, OkHttp y autenticación HTTP | «Integra una API REST» |
| [android-datastore](android-datastore/SKILL.md) | Preferencias y migraciones locales | «Persiste idioma y preferencias» |
| [android-offline-sync](android-offline-sync/SKILL.md) | Caché, sincronización y conflictos | «Define edición sin conexión» |
| [android-auth-session](android-auth-session/SKILL.md) | Identidad, perfil y cierre de sesión | «Implementa acceso con Firebase Auth» |
| [android-firestore](android-firestore/SKILL.md) | Esquema, reglas, índices y consultas | «Protege documentos por propietario» |
| [android-cloud-functions](android-cloud-functions/SKILL.md) | Backend confiable y transacciones | «Crea una operación callable» |
| [android-security-privacy](android-security-privacy/SKILL.md) | Secretos, permisos y datos personales | «Revisa seguridad antes del release» |
| [android-ai-integration](android-ai-integration/SKILL.md) | IA, cupos y alternativa local | «Integra extracción con IA desde backend» |
| [android-documents](android-documents/SKILL.md) | Importación, OCR y PDF | «Agrega importar y exportar documentos» |
| [android-notifications](android-notifications/SKILL.md) | Canales, permisos y navegación | «Abre el detalle desde una notificación» |
| [android-observability](android-observability/SKILL.md) | Analytics, Crashlytics y diagnóstico | «Instrumenta eventos sin datos personales» |
| [android-remote-config](android-remote-config/SKILL.md) | Flags, compatibilidad y actualización | «Controla una función remotamente» |
| [android-billing](android-billing/SKILL.md) | Compras y derechos verificados | «Diseña una suscripción verificable» |
| [android-unit-testing](android-unit-testing/SKILL.md) | Dominio, ViewModels y repositorios | «Prueba errores y concurrencia del guardado» |
| [android-ui-testing](android-ui-testing/SKILL.md) | Compose, navegación y dispositivo | «Verifica el flujo de login a inicio» |
| [android-backend-testing](android-backend-testing/SKILL.md) | Reglas, emuladores y Functions | «Prueba aislamiento entre cuentas» |
| [android-performance](android-performance/SKILL.md) | Inicio, memoria y recomposición | «Mide y mejora esta pantalla» |
| [android-ci](android-ci/SKILL.md) | Pipeline, checks y artefactos | «Automatiza validaciones en CI» |
| [android-release](android-release/SKILL.md) | Firma, AAB y publicación Android | «Prepara un candidato de release» |
| [android-backend-deploy](android-backend-deploy/SKILL.md) | Entornos, secretos y despliegue Firebase | «Prepara el backend de producción» |
| [android-maintenance](android-maintenance/SKILL.md) | Actualizaciones, incidentes y evolución | «Planifica esta migración del stack» |

## Reglas de uso del paquete

- Las versiones documentadas de CvFree son evidencia local, no una recomendación de versiones actuales. Al crear el siguiente proyecto, verifica compatibilidad en documentación oficial y registra las versiones que realmente compilan.
- Las integraciones de IA, PDF/OCR, Billing, notificaciones, API REST y sincronización avanzada son opcionales. No añadas sus dependencias si el producto no las necesita.
- Los comandos con variantes `DevFreeDebug` y `ProdFreeRelease` ilustran la convención de CvFree. Confirma las tareas del nuevo proyecto con `./gradlew tasks --all` o `.\gradlew.bat tasks --all` antes de ejecutarlos.
- Preparar un release o un despliegue no demuestra que se haya publicado. Reporta por separado artefactos generados, pruebas locales y comprobaciones remotas realmente realizadas.
- No copies credenciales, archivos de firma, datos de usuarios ni configuraciones de producción del proyecto original.
