---
name: android-maintenance
description: Planificar actualizaciones, migraciones e incidentes de un proyecto Android multimódulo y Firebase. Usar para evolucionar el stack o resolver regresiones sin perder compatibilidad.
---

# Mantener y evolucionar

## Procedimiento

1. Identifica el problema observado, versiones instaladas y alcance. Consulta notas oficiales de los componentes que cambien; distingue actualización necesaria de limpieza opcional.
2. Para migraciones, registra compatibilidad de toolchain, procesadores, Compose, Firebase y backend. Cambia grupos relacionados en pasos verificables en vez de actualizar todo sin diagnóstico.
3. Versiona esquemas y conserva lectura de datos anteriores mientras existan clientes compatibles. Evita migraciones destructivas sin una estrategia explícita de conservación/recuperación.
4. En incidentes, reproduce con datos controlados, recoge señales sin contenido privado y limita el cambio a la causa. Considera flags y rollout como mitigación cuando existan.
5. Actualiza decisiones de arquitectura, instrucciones de ejecución y ownership cuando cambie el comportamiento real. El código y los builds prevalecen sobre README obsoletos.
6. Retira dependencias y parámetros muertos después de comprobar consumidores, versiones antiguas y configuración remota.

## Comprobar y entregar

Entrega causa o motivación, cambio, pruebas dirigidas y riesgos residuales. Registra migraciones pendientes y compatibilidad de clientes anteriores. Una corrección local no cierra un incidente remoto hasta verificar su entrega y señales de recuperación.
