---
name: android-observability
description: Instrumentar Analytics, Crashlytics y logging con contratos estables y protección de datos. Usar para medir flujos o diagnosticar fallos Android y backend.
---

# Instrumentar comportamiento y fallos

## Procedimiento

1. Define preguntas operativas antes de eventos: conversión, fallo, duración y abandono. Usa nombres y parámetros de catálogo de cardinalidad acotada.
2. Expón un contrato de reporter sustituible para pruebas. Registra pantalla desde navegación y acciones en un único punto para evitar duplicados.
3. Separa resultado de negocio, validación y excepción técnica. No envíes nombres, correos, documentos, texto libre, tokens ni URLs privadas.
4. Revisa mensajes, causas y excepciones anidadas antes de enviarlas a Crashlytics; conserva información técnica útil sin contenido del usuario.
5. Inicializa logging y reporters de forma coherente, controla recopilación según ambiente y decisiones de privacidad del producto.
6. Define diagnóstico remoto y alertas sobre señales útiles. Un log de envío local no demuestra recepción en Analytics ni registro en Crashlytics.

## Comprobar y entregar

Usa reporter en memoria para contratos y una sesión controlada para verificar recepción remota cuando esté autorizada. Entrega catálogo de eventos, política de privacidad de parámetros y evidencia separada de verificación local/remota.
