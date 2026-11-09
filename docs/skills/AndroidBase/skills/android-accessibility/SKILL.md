---
name: android-accessibility
description: Revisar y corregir accesibilidad de interfaces Compose con TalkBack, escalado y navegación por foco. Usar para hacer un flujo utilizable con tecnologías de asistencia.
---

# Hacer accesible un flujo

## Procedimiento

1. Recorre el flujo con TalkBack y registra qué anuncia, qué recibe foco y qué acciones son inaccesibles.
2. Añade etiquetas, roles y descripción de estado a controles que lo necesiten. Los iconos decorativos no deben duplicar el texto anunciado.
3. Respeta tamaños táctiles accesibles, contraste y foco visible. No comuniques error, selección o éxito solamente con color.
4. Prueba fuente ampliada, pantalla pequeña, textos largos, teclado y orientación. Evita recortes que oculten acciones esenciales.
5. Revisa agrupación semántica, orden de lectura, diálogos, mensajes de error y anuncios de cambios importantes sin ruido repetido.
6. Añade comprobaciones de semántica para regresiones críticas. Distingue lo automatizado de la revisión manual con lector de pantalla.

## Comprobar y entregar

Entrega problemas observados, cambios y recorrido verificado. Una persona debe poder completar la acción principal sin depender de posición visual, color o gestos no anunciados. No declares accesibilidad completa basándote solo en previews.
