---
name: android-compose-ui
description: Construir pantallas Jetpack Compose con estado elevado, lifecycle y efectos controlados. Usar al implementar o corregir interfaces Android.
---

# Construir pantallas Compose

## Ubicacion y nombres

Coloca cada pantalla en `ui/<pantalla>/`, con archivo y composable `<Pantalla>Screen`. Usa el mismo prefijo para su ViewModel y contratos: por ejemplo `LoginViewModel`, `LoginUiState` y `LoginUiIntent`. Los contratos de estado, intenciones y errores tienen archivos propios; no se declaran dentro del archivo de pantalla ni del ViewModel. Actualiza previews, recursos R e imports de navegacion al mover paquetes.

## Procedimiento

1. Separa la entrada que obtiene ViewModel/estado de la pantalla que recibe valores y callbacks. Usa `collectAsStateWithLifecycle` para observación Android cuando corresponda.
2. Modela contenido, carga, vacío, error y reintento. Mantén formularios utilizables con teclado, scroll e insets del sistema.
3. Usa `remember` para estado efímero y `rememberSaveable` para valores pequeños recuperables. La información de negocio importante necesita propietario y persistencia explícitos.
4. Ejecuta efectos con claves estables y propósito claro; retira observers mediante `DisposableEffect`. No hagas red, guardados ni navegación en el cuerpo de un composable.
5. Usa listas lazy con claves estables y componentes acotados. Calcula estado derivado cuando reduzca trabajo o invalide correctamente; no agregues optimizaciones sin evidencia.
6. Soporta anchos disponibles y orientación sin dimensiones rígidas que corten contenido. Ofrece previews con datos controlados y sin SDK remoto.

## Comprobar y entregar

Verifica rotación, back, teclado, recomposición y recuperación de errores. La pantalla debe poder renderizarse y probarse con estado/callbacks sin iniciar servicios reales.

## Argumentos de llamadas

Las llamadas Kotlin con tres o mas argumentos suministrados usan nombres explicitos en todos los argumentos entre parentesis, incluidos constructores y callbacks de pantallas: `LoginScreen(state = state, onIntent = viewModel::onIntent, ...)`. Las llamadas largas de pantallas se distribuyen con un argumento por linea. La lambda final idiomatica de Compose puede quedar fuera de los parentesis y cuenta para el umbral. Conserva varargs posicionales y no fuerces argumentos con nombre en APIs Java o valores de tipo funcion.

## Referencias de entrada y contraste

Splash y los tres pasos del onboarding siguen las capturas oscuras y claras de Stitch en docs. El paso 2 muestra el candado con orbitas y fuentes de colores; el paso 3 muestra saldo, dos pagos de ejemplo (Netflix y Luz del Sur), tasa de ahorro y barra de progreso. Sus ilustraciones viven en `ui/onboarding/components/` y los previews cubren ambos temas. Los iconos de proveedor conservan morado y celeste en ambos temas con simbolos y etiquetas blancos; usa los colores accesibles de `EntryColors` (contraste de etiquetas blancas >= 4.5:1). Los textos generales usan `onSurface`/`onBackground`: no pongas blanco sobre un fondo claro. Un contenedor con color propio declara explicitamente su color de contenido. Los ejemplos visuales no afirman una sincronizacion real ni cifrado pendiente de implementar.

## Estilo de enlaces y proveedores de Auth

`AccountLink`, en core-designsystem, centra el texto de contexto y destaca solo la accion: amarillo y negrita en oscuro; texto oscuro, negrita y subrayado en claro. Se reutiliza en Login, Registro, recuperacion y el acceso desde Onboarding. Los textos completos siguen en los recursos de cada feature y el area tactil mantiene un minimo de 48 dp.

El boton de Google conserva el simbolo multicolor como vector local sin tintado. La huella usa un simbolo verde sobre circulo menta en claro y verde profundo en oscuro; sus contenedores son claros de dia y oscuros con borde de noche. Estos cambios son visuales: no habilitan OAuth ni biometria real. Registro separa el checkbox y el texto de terminos con 12 dp y agrega espacio vertical. No se modifica el flujo de recuperacion ni se implementa OTP al adoptar sus referencias oscuras.

## Pantalla completa de recuperacion

Recuperacion usa las referencias claras y oscuras `recuperar_contrase_a` y `recuperar_contrase_a_modo_oscuro`, sin toolbar ni app bar superior: acceso circular de regreso, distintivo de seguridad, anillo dorado con candado y llave verde, correo con sobre y etiqueta externa, aviso con reloj, boton con flecha, tarjeta de proteccion con escudo y pie de regreso al acceso. Los elementos visuales propios se agrupan en `auth/ui/forgotpassword/components/RecoveryVisuals.kt` y usan superficies, bordes e iconos adaptados al tema.

El campo de correo tiene etiqueta accesible, teclado de email y accion IME. La pantalla conserva scroll e insets; hereda la apariencia global y vuelve al Login para cambiarla. El titulo y el aviso corresponden a recuperacion por enlace, sin prometer codigos de seis digitos, caducidad de quince minutos ni compatibilidad bancaria no implementada. Firebase mantiene el envio de enlaces y el modo local conserva su aviso de operacion ilustrativa.
