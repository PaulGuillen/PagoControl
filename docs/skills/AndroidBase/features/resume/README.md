# Guardado automático del CV

La opción de Ajustes está activada de forma predeterminada y conserva su elección entre reinicios. Los cambios se guardan localmente mediante `ResumeRepository`, después de una pausa de 500 ms al escribir y al salir de una sección o enviar la app al fondo.

Se conservan también los campos incompletos. Los CV pueden retomarse desde Mis CV; la validación habitual sigue aplicándose al confirmar cada formulario. Cancelar una entrada descarta los cambios pendientes de esa entrada.

Al completar una importación desde PDF o escaneo, el CV importado se guarda automáticamente aunque se retroceda sin editar ningún campo. Si el guardado automático está desactivado, la importación queda en el editor hasta guardarla manualmente o activar la opción.

Las escrituras automáticas, el guardado manual y la eliminación se procesan en orden. El guardado automático no genera notificaciones. Si se desactiva, el botón Guardar CV continúa funcionando.

El borrador activo también se conserva mediante `SavedStateHandle` para mantener sus datos e identificador cuando Android recrea el proceso. Si falla una escritura automática, se muestra un aviso y la siguiente edición permite intentarla de nuevo.

## Validación

- Pruebas unitarias: `gradlew.bat :features:resume:testDevDebugUnitTest :features:settings:testDevDebugUnitTest`.
- Pruebas de formularios: `gradlew.bat :features:resume:connectedDevDebugAndroidTest`.

Las pruebas de formularios utilizan el paquete independiente `com.devpaul.features.resume.test`.
