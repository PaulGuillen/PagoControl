---
name: android-release
description: Preparar firma, versión, AAB y entrega Android a Google Play con comprobaciones de producción. Usar para generar un candidato o realizar una publicación solicitada.
---

# Preparar y entregar Android

## Procedimiento

1. Identifica variante distribuible, applicationId, ambiente, versionCode/versionName y canal. Confirma tareas reales; no construyas una combinación deshabilitada.
2. Verifica requisitos actuales de Play en documentación oficial cuando la tarea incluya publicación. Define compatibilidad SDK y notas según la app concreta.
3. Provee firma mediante almacenamiento seguro/CI; nunca incrustes contraseña ni copies keystores al repositorio. Distingue clave de carga y firma administrada por Play.
4. Ejecuta pruebas/lint relevantes y genera AAB release. Verifica optimización/R8, recursos, mapping de Crashlytics y ausencia de herramientas/proveedores debug.
5. Comprueba endpoints, proyecto Firebase, App Check, derechos y flags del ambiente final. Realiza smoke test de un artefacto representativo de distribución.
6. Prepara ficha, privacidad, Data safety y canal interno según alcance. Publica solo cuando la solicitud incluya esa acción; conserva un resultado concreto revisable si solo se pidió preparación.

## Comprobar y entregar

Entrega ruta del AAB, variante, versión, checks y estado real de carga/publicación. Para incidentes, contempla detener rollout o entregar una corrección con versionCode superior; no presupongas downgrade instalado.
