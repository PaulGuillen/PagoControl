---
name: android-ai-integration
description: Integrar funciones de IA desde Android mediante backend con validación, cuotas y alternativa local. Usar para extracción o transformación de contenido asistida por modelos.
---

# Integrar IA con control del servidor

## Procedimiento

1. Define valor para el usuario, datos enviados y elección explícita de usar IA cuando corresponda. Mantén la decisión durante selectores y navegación.
2. Llama al proveedor desde backend con secretos y autorización allí. El cliente no determina derechos comerciales ni cuotas enviando un booleano.
3. Diseña consulta de disponibilidad y consumo transaccional separados. Define reserva, vencimiento, éxito que consume cuota y fallos que la liberan.
4. Trata texto importado y respuesta del modelo como datos no confiables. Limita tamaño, tiempo y esquema; valida campos antes de guardarlos o renderizarlos.
5. Ofrece extracción local o reintento cuando tenga sentido. No eludas el backend llamando directamente al modelo si falla el cupo.
6. No registres documentos/prompts completos. Explica procesamiento, retención y límites de identificación; un identificador enviado por el cliente no prueba un dispositivo físico.

## Comprobar y entregar

Prueba salida inválida, timeout, cupo agotado, concurrencia, cancelación y proveedor no disponible. Distingue respuesta local de respuesta mejorada con IA. Verifica modelos y precios actuales solo cuando sea necesario elegir proveedor/modelo.
