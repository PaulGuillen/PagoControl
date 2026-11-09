---
name: android-project-map
description: Mapear alcance, stack, arquitectura y plan de implementación de una nueva app Android basada en Kotlin y Compose. Usar al iniciar un proyecto o revisar su diseño general.
---

# Mapear el proyecto

## Procedimiento

1. Identifica usuarios, acciones principales, datos sensibles y el flujo mínimo que debe funcionar. Avanza con supuestos explícitos cuando falten decisiones reversibles.
2. Revisa el repositorio si existe; diferencia código implementado, dependencia declarada y funcionalidad pendiente. Para entender el origen del paquete, lee [la fotografía de CvFree](references/cvfree-baseline.md).
3. Propón módulos por responsabilidad y propietario de cada contrato. Dibuja la dirección de dependencias y el flujo UI → dominio → contrato ← infraestructura.
4. Decide cuáles servicios necesita el producto: Auth, Firestore, Functions, API REST, documentos, IA, compras o notificaciones. Explica por qué se incluyen; no actives todos por defecto.
5. Registra decisiones con contexto, alternativas y consecuencias. Ordena hitos por flujos completos que puedan compilarse y demostrarse.

## Entregables y cierre

Entrega mapa de módulos, stack verificado, decisiones abiertas, ambientes, estrategia de pruebas y una hoja de ruta con criterios de aceptación. El primer hito debe permitir ejecutar una pantalla con datos controlados; los siguientes incorporan infraestructura real sin rehacer contratos.
