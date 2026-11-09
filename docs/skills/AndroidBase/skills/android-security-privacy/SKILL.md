---
name: android-security-privacy
description: Revisar seguridad y privacidad de una app Android y su backend antes de integrar datos sensibles o publicar. Usar en auditorías o cambios que alteren límites de confianza.
---

# Revisar límites de confianza

## Procedimiento

1. Mapea datos personales, puntos de entrada y quién autoriza operaciones. Vincula cada riesgo observado con código o configuración concretos.
2. Busca secretos en app, historial relevante, logs y artefactos. Una API key de configuración Firebase no equivale por sí sola a una credencial privilegiada; revisa restricciones y permisos reales.
3. Coloca secretos de proveedores privados en backend. Verifica Auth, reglas e IAM; App Check complementa estos controles.
4. Revisa componentes exportados, intents, proveedores de archivos, permisos mínimos y validación de URI/payload externo.
5. Revisa aislamiento entre cuentas, retención, borrado, backups y datos enviados a Analytics/Crashlytics/IA. Un hash persistente puede seguir siendo un identificador seudónimo.
6. Comprueba diferencia entre debug/release y evita credenciales o proveedores de depuración en producción. Consulta requisitos oficiales vigentes si el trabajo incluye obligaciones legales o políticas de tienda.

## Comprobar y entregar

Entrega hallazgos reproducibles con impacto y corrección, distinguiendo riesgo demostrado de hipótesis. Verifica fallos de autorización con identidades controladas y que los cambios no oculten errores necesarios para diagnóstico.
