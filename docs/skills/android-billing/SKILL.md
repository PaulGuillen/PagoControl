---
name: android-billing
description: Diseñar e implementar compras Google Play Billing y derechos de acceso verificados por servidor. Usar para productos o suscripciones reales de una app Android.
---

# Implementar acceso comercial

## Procedimiento

1. Verifica documentación oficial vigente de Billing y configuración real de productos, ofertas, base plans y cuentas de prueba. No deduzcas productos activos por constantes del APK.
2. Separa estado de compra, derechos efectivos y UI del plan. Un flavor Premium Debug sirve para presentación de prueba, no acredita una compra.
3. Gestiona conexión, consulta de productos, lanzamiento y actualizaciones con lifecycle. Define pendientes, cancelación, errores y restauración.
4. Verifica tokens y estado de compra en backend antes de conceder derechos. Diseña reconocimiento de compras verificadas y deduplicación según el tipo de producto y requisitos actuales.
5. Sincroniza renovaciones, expiración, reembolso y revocación; contempla notificaciones de servidor e idempotencia cuando entren en alcance.
6. No muestres éxito comercial solo porque abrió el diálogo. Mantén derechos confiables ante reinstalación, cambio de dispositivo y caché antigua.

## Comprobar y entregar

Prueba con mecanismos de prueba de Play apropiados y verifica ambos lados del circuito. Entrega estados, backend de verificación y matriz de pruebas; si solo existe interfaz del plan, descríbela como tal.
