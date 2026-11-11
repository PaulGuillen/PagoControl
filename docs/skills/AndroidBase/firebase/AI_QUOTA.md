# Una importación con IA cada 7 días por dispositivo

El cupo Free es único para escaneo y PDF: un procesamiento con IA completado cada 7 × 24 horas. La importación local, el OCR y la recuperación de PDF con datos internos de CvFree no consumen cupo. No se aplica un límite por cuenta: cambiar de usuario en el mismo dispositivo conserva el cupo compartido.

## Funcionamiento

Android envía una huella SHA-256 de `cvfree:` y `Settings.Secure.ANDROID_ID`, el texto reconocido y el borrador. No se envía el archivo original ni el identificador Android sin transformar. `ANDROID_ID` normalmente se mantiene al reinstalar con el mismo usuario y firma. Cambiar de perfil de Android, firma o restaurar el dispositivo puede cambiarlo. La huella es un identificador seudónimo, no un identificador anónimo.

La función invocable `enhanceResume`, en `us-central1`, exige Firebase Authentication y App Check. Reserva el dispositivo en una transacción de Firestore antes de llamar a Gemini y usa únicamente el reloj del servidor. Dos solicitudes simultáneas no pueden ejecutar IA para el mismo cupo. Una respuesta completa y válida inicia los 7 días; los errores liberan la reserva. Las reservas abandonadas expiran después de 5 minutos. Si el cliente cancela o pierde conexión después de que el servidor complete la IA, el uso sí se conserva.

Firestore guarda en `ai_device_quotas/{deviceHash}` solo `lastSuccessAtMs`, `reservationId` y `reservedUntilMs`. La operación no guarda texto del CV, correo ni UID en esa colección. El contenido se procesa transitoriamente en la función y en Gemini. No se registran cuerpos de solicitudes ni errores con contenido del documento.

Cuando el cupo está agotado, Android informa la fecha y hora de disponibilidad y continúa con el resultado local. Ante una reserva activa, falta de conexión, servicio sin desplegar, fallo de App Check o error de IA, informa que continúa sin IA. Nunca intenta la llamada directa a Gemini como alternativa al límite.

Al abrir las opciones de importación, Android consulta `getResumeAiQuota`, una función invocable de solo lectura que exige autenticación y App Check. Consultar el estado no consume cupo ni llama a Gemini. La interfaz solo ofrece asistencia de IA cuando el servidor devuelve `available`; distingue comprobación en curso, cupo agotado, reserva activa y servicio no verificable. El plazo mostrado procede del servidor, no del reloj del teléfono.

La decisión de usar IA se conserva durante el selector de archivos o el escáner y se transmite a la ruta de importación. Si se elige continuar sin IA, el importador no envía el texto al servicio aunque el cupo cambie después. Una importación realmente mejorada muestra un aviso de uso de IA; la extracción local y los PDF con datos internos de CvFree no muestran ese aviso ni consumen cupo.

## Activación en Firebase

Estos archivos preparan un servicio nuevo; no contienen credenciales ni despliegan cambios automáticamente. Se necesita un proyecto con Cloud Functions habilitado, facturación compatible con su despliegue, Firestore y una clave de Gemini API autorizada para el modelo elegido.

Ejecuta desde `firebase/`, sustituyendo `TU_PROJECT_ID` por el proyecto del `google-services.json` de la app:

```powershell
npm --prefix functions ci
npm --prefix functions test
firebase login
firebase functions:secrets:set GEMINI_API_KEY --project TU_PROJECT_ID
firebase deploy --only functions:resume-ai --project TU_PROJECT_ID
```

La CLI solicita `AI_MODEL_NAME` al desplegar. Configura un modelo disponible en tu proyecto que admita `generateContent` y salida JSON estructurada. No copies claves a Android ni al repositorio. `GEMINI_API_KEY` queda en Secret Manager; `AI_MODEL_NAME` es una configuración del servidor. El antiguo parámetro Android `ai_model_name` no selecciona el modelo de esta función.

Despliega ambas funciones del codebase `resume-ai`: `enhanceResume` y `getResumeAiQuota`. Si falta cualquiera de ellas, no se puede confirmar el flujo completo. Una respuesta HTTP 404 en la URL invocable indica que ese endpoint no está disponible; que la importación local termine correctamente no demuestra que se haya utilizado IA.

Si falta Firebase CLI, puede instalarse con `npm install -g firebase-tools` o ejecutarse como `npx firebase-tools`. El runtime desplegado es Node.js 22. Repite el despliegue y la configuración de secretos para cada proyecto dev/prod que corresponda.

Integra `ai-quota.rules` dentro del match de documentos de las reglas existentes de Firestore. Conserva las reglas para usuarios, calificaciones y opiniones. No reemplaces las reglas completas con este fragmento. Revisa especialmente las reglas generales: una regla amplia que permita escribir en cualquier documento también permitiría alterar el cupo aunque este fragmento lo deniegue. Solo el Admin SDK de la función debe modificar esta colección. Este `firebase.json` despliega únicamente Functions para evitar sobrescribir reglas desconocidas.

Registra la app en App Check y habilita Play Integrity para Release. En Debug, registra el token del proveedor de depuración en la consola; no lo publiques. El backend exige un token válido también en desarrollo.

La app deja de usar Firebase AI Logic directamente. Para cerrar la ruta de acceso de APK anteriores, revisa y deshabilita el acceso directo a Firebase AI Logic si ninguna otra app del proyecto lo necesita, y configura la actualización obligatoria según tu estrategia de versiones. La clave del backend debe ser independiente de claves distribuidas en APK anteriores.

## Premium y configuración de acceso

Solo un custom claim de Firebase Auth `premium: true`, otorgado desde un entorno de administración confiable, permite omitir el cupo semanal. El servidor no acepta un plan ni un booleano enviados por Android. Una variante Premium Debug por sí sola no otorga este permiso: para probar la exención, usa una cuenta de pruebas con ese claim y renueva su token de autenticación. Esto no activa compras ni implementa la validación comercial de suscripciones.

`ai_import_enabled` sigue controlando si la app intenta IA. `premium_paywall_enabled` ya no retira a Free su uso semanal. El cupo servidor es fijo de un uso: los valores antiguos `free_ai_scans` y `premium_monthly_limit` del cliente no autorizan solicitudes adicionales ni implementan límites en el servidor.

## Límites de la identificación

App Check y autenticación reducen solicitudes ajenas a la app, pero no certifican que la huella enviada corresponda físicamente al dispositivo. Un cliente comprometido podría enviar otra huella. Esta solución limita el uso normal por dispositivo y resiste reinstalaciones habituales, cambios de cuenta y de reloj; no garantiza protección contra dispositivos modificados. Si necesitas garantías adicionales, evalúa controles complementarios de cuenta y Play Integrity en el servidor.

## Validación

```powershell
npm --prefix functions test
..\gradlew.bat :features:resume:testDevDebugUnitTest :app:testDevFreeDebugUnitTest :app:assembleDevFreeDebug
```

Las pruebas del backend verifican el límite exacto de 7 días, concurrencia, fallo y recuperación, reservas expiradas, dispositivos independientes, Premium confiable y validación de entradas/respuestas. Utilizan un almacén transaccional en memoria; no sustituyen una prueba de integración con Firestore desplegado.

Tras el despliegue, prueba con una cuenta Free: importa un PDF externo legible con IA; repite desde escaneo y comprueba que continúa sin IA; cambia de cuenta y reinstala con la misma firma para comprobar que conserva el cupo. Comprueba la devolución ante un fallo real de Gemini y que los clientes no puedan modificar `ai_device_quotas`. Una clave de dispositivo de prueba independiente permite probar el primer uso sin eliminar registros reales. No adelantes el reloj del teléfono: el cálculo usa el servidor.

Referencias: [funciones invocables](https://firebase.google.com/docs/functions/callable), [App Check](https://firebase.google.com/docs/app-check/cloud-functions), [Secret Manager](https://firebase.google.com/docs/functions/config-env), [ANDROID_ID](https://developer.android.com/about/versions/oreo/android-8.0-changes).
