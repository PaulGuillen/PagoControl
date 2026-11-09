---
name: android-ci
description: Crear pipelines reproducibles de validación y generación de artefactos Android y Firebase. Usar para automatizar pruebas, lint y builds en el proveedor CI elegido.
---

# Automatizar validación

## Procedimiento

1. Revisa proveedor y convenciones existentes. Si no hay pipeline, define checks para PR y ramas protegidas según el flujo del equipo.
2. Configura JDK/SDK y Node según versiones verificadas. Usa wrapper y lockfiles; configura cachés sin incluir secretos ni arrastrar archivos de firma.
3. Ejecuta pruebas de dominio/features, lint y build de una variante real. Añade Functions/reglas cuando cambien sus archivos y sus herramientas estén disponibles.
4. Usa configuración Firebase de pruebas o un mecanismo explícito de provisión; no omitas silenciosamente pasos porque falta un archivo local.
5. Separa validación de PR de firma/publicación. No expongas secretos en contribuciones no confiables; restringe permisos de tokens y acciones externas.
6. Publica reportes y artefactos con variante, revisión y retención. Un build de CI no es automáticamente una entrega a la tienda.

## Comprobar y entregar

Valida sintaxis y ejecuta localmente comandos equivalentes cuando sea posible. Entrega pipeline y lista de secretos/configuración requeridos por nombre. Si no se ha ejecutado remotamente, indícalo sin declarar CI verde.
