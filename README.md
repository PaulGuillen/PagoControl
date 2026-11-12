# PagoControl

Base Android modular con Kotlin, Jetpack Compose, MVVM/MVI, Koin y navegación tipada.

Estructura basada en `docs/skills/AndroidBase` y la skill `android-modular`: `app`, `buildSrc`, `core-domain`, `core-data`, `core-designsystem`, `core-platform`, `navigation`, `session` y las features `splash`, `onboarding`, `auth`, `home`, `movements`, `reports` y `profile`. Las convenciones Gradle se centralizan en `buildSrc/src/main/kotlin/plugin/`.

Implementa Login, Registro y recuperación por correo con Firebase Authentication y perfiles en Cloud Firestore. El arranque sigue AndroidBase con Timber, Analytics/Crashlytics, App Check y Koin, conservando la UI de PagoControl y sus temas claro, oscuro y del sistema. Inicio utiliza notificaciones locales y movimientos confirmados en Room. Movimientos y Reportes mantienen datos ilustrativos y todavía no comparten este historial.

## Notificaciones en Inicio

Inicio tiene dos pestañas: «Billeteras digitales» y «Correo». Billeteras muestra «Cobros recibidos este mes», cantidad de cobros, apps seleccionadas, gráficos de ingresos y cobros recientes. No presenta un saldo neto ni una tarjeta de egresos. El importe principal es el ingreso del mes actual; los gráficos y el historial de cobros excluyen importes negativos, conservando los movimientos existentes en Room. Correo muestra balance registrado, ingresos y egresos del mes, una tarjeta propia de Gmail, gráfico de cobros por semana/mes y transacciones recientes.

En Billeteras, «Elegir apps a monitorear» muestra los iconos reales y nombres de las apps seleccionadas, con un acceso «Añadir». Al pulsar se abre un selector con búsqueda, iconos y selección por fila. Los iconos se cargan localmente desde las apps instaladas, sin conexión. Si falta el permiso, «Activar monitoreo» abre los ajustes de Android. Desde ese momento se guardan nuevas notificaciones de las apps seleccionadas, incluso en segundo plano. Gmail no aparece en esta lista: se activa desde Correo.

En Correo, activa «Monitorear Gmail». La tarjeta muestra el icono instalado de Gmail y «Monitoreando Gmail» solo cuando la opción y el permiso de Android están habilitados. Si Gmail falta o está deshabilitado, no permite activar la opción. Se capturan únicamente notificaciones nuevas del paquete de Gmail con palabras relacionadas con pagos, compras, cobros o transferencias; se ignoran mensajes sin esos indicios. Es un filtro de candidatos, no una verificación bancaria: los avisos quedan pendientes para confirmar importe en soles y si corresponde a ingreso o egreso, o descartarlos. Una transferencia recibida puede registrarse como ingreso y una enviada como egreso. Los recordatorios, promociones y duplicados deben descartarse. La captura puede ser incompleta porque depende del contenido que Gmail entregue a Android.

No se conecta una cuenta mediante OAuth, no se consulta la API de Gmail y no se importa el historial del buzón. La elección de monitorear Gmail se guarda localmente y desactivarla detiene capturas futuras conservando el historial. Sus pendientes, totales, gráficos y movimientos están separados de Billeteras en todas las consultas; no se suman entre pestañas. Una misma operación podría aparecer en ambas fuentes y no hay reconciliación automática entre ellas.

Los nuevos avisos de la app Yape con título «Confirmación de Pago» y texto reconocido «[nombre] te envió un pago por S/ [importe]» se registran automáticamente como ingresos, también cuando incluyen el código de seguridad. Se muestra el remitente sin etiquetas adicionales de lectura. Los demás formatos y apps quedan pendientes: revisa la operación, corrige el importe en soles y confirma ingreso o egreso, o descártalo. La regla comprueba el paquete de Yape y vuelve a extraer el importe del mensaje; no basta con que otra app se llame Yape. Los pendientes anteriores se mantienen para revisión manual.

Los ingresos automáticos y los confirmados afectan el resumen de cobros. No representa el saldo de una cuenta bancaria. El gráfico semanal usa lunes a domingo y el mensual agrupa días 1–7, 8–14, 15–21, 22–28 y 29–31. Los avisos pendientes de Billeteras permiten confirmar un ingreso o descartar el mensaje.

Room guarda selecciones, pendientes y confirmados por dispositivo, independientemente de la cuenta de Firebase. Cambiar de cuenta no cambia esta información. La captura y consulta no requieren Firestore; el acceso a las pantallas conserva el inicio de sesión existente. La base de notificaciones se excluye del respaldo automático en la nube. Desmarcar una app detiene su captura futura y conserva su historial.

Se deduplican entregas del mismo evento Android (clave y fecha de publicación). No hay reconciliación automática de una misma operación notificada por distintas apps: comprueba los movimientos antes de confirmar. Los textos son los que Android entregue y pueden estar incompletos; solo el formato de ingreso de Yape reconocido tiene registro automático. Promociones, recordatorios, rechazos, egresos y mensajes ambiguos requieren revisión.

## Ejecutar

Abrir en Android Studio, configurar Android SDK 37 y JDK 21 y ejecutar `app`, o generar el APK:

```powershell
.\gradlew.bat :app:assembleDebug
```

En la primera apertura puedes avanzar u omitir la introducción. Crea una cuenta con correo y contraseña o inicia sesión con una cuenta existente. Google y huella quedan pendientes. Consulta [la configuración de Firebase y las pruebas con emuladores](docs/FIREBASE_SETUP.md); las cuentas bancarias no están conectadas.

Consulta [la arquitectura y validaciones](docs/ARCHITECTURE.md) y [las skills de referencia](docs/skills/README.md).
