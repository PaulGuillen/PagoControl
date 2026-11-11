# Calificaciones de CV Free

El almacenamiento de currículums por cuenta, la migración de DataStore y las reglas
de la subcolección `users/{uid}/resumes` se documentan en [RESUMES.md](RESUMES.md).

Firestore guarda la última calificación en `app_ratings/{uid}` y un historial inmutable en
`app_ratings/{uid}/submissions/{submissionId}`. Ambos documentos se escriben en una sola
transacción. Los campos son `uid`, `stars` (1–5), `comment` (máximo 500 caracteres),
`submittedAt` (timestamp del servidor) y `submissionId`.

El límite es de un envío cada 24 horas por UID, compartido entre dispositivos. Se requiere
conexión para consultar y enviar. El cliente comprueba la última fecha para mostrar la alerta;
las reglas usan la hora del servidor para imponer el límite aunque se cambie el reloj del dispositivo.

## Configuración necesaria en Firebase

Este repositorio no contiene las reglas actualmente publicadas ni una configuración para desplegar las reglas completas.
En cada proyecto Firebase usado por las variantes dev/prod, integra el bloque de `ratings.rules`
dentro de `service cloud.firestore { match /databases/{database}/documents { ... } }`
de tus reglas existentes y publícalas desde Firebase Console. Conserva las reglas existentes
para las demás colecciones. No publiques `ratings.rules` como archivo completo: es un bloque
para integrar, no reemplaza las reglas de toda la base de datos.

Comprueba que ninguna regla general permita escribir sin restricciones en `app_ratings`,
porque los permisos de reglas coincidentes se combinan con OR.

Verifica con dos sesiones del mismo UID: el primer envío debe guardar ambos documentos;
un segundo envío antes de 24 horas debe rechazarse y mostrar la alerta. Otro UID debe poder
enviar su propia calificación, pero no leer ni modificar las de otro usuario. Las actualizaciones
y eliminaciones del historial deben rechazarse, al igual que valores fuera de 1–5 estrellas,
comentarios de más de 500 caracteres y envíos sin autenticación.

Referencia: https://firebase.google.com/docs/firestore/manage-data/transactions

La configuración de Cloud Functions para el cupo semanal de IA se documenta por separado en [AI_QUOTA.md](AI_QUOTA.md). Su despliegue no publica reglas de Firestore.

## Opiniones sobre el futuro plan Premium

La pantalla Mi plan muestra el plan de la variante: Free o Premium de prueba en Premium Debug.
El futuro plan comercial Premium aparece en construcción. El formulario
pregunta por la utilidad de la propuesta (1 a 5 estrellas) y permite un comentario opcional.
No inicia compras ni suscripciones. La configuración de ambientes dev/prod se mantiene.

Estas respuestas se guardan por separado en `premium_plan_feedback/{uid}` y en
`premium_plan_feedback/{uid}/submissions/{submissionId}`, con los mismos campos y garantías
de transacción que las calificaciones. Su límite de 24 horas es independiente de `app_ratings`.

Integra también el bloque de `premium-feedback.rules` dentro del match de documentos de tus
reglas existentes en cada proyecto Firebase dev/prod. No reemplaces las reglas completas.
Hasta publicar esas reglas, el formulario puede devolver un error de permisos y conservará
la opinión para reintentar. La colección se crea automáticamente con el primer envío aceptado.

Comprueba que un usuario autenticado pueda enviar ambas opiniones el mismo día, que no pueda
leer o modificar las de otro UID y que los duplicados, valores inválidos y envíos sin autenticar
se rechacen. No se incluyen correo, nombre ni contenido del CV en estas respuestas.
