---
name: android-network
description: Integrar APIs HTTP con Retrofit y OkHttp en Android. Usar al implementar endpoints, interceptores, autenticación y manejo de fallos de red.
---

# Integrar una API REST

## Procedimiento

1. Lee el contrato real del servicio: endpoints, autenticación, códigos, esquema, timeout y límites. Define base URL por ambiente fuera de pantallas.
2. Configura Retrofit, un convertidor coherente y OkHttp en infraestructura. Mantén DTO y modelos de dominio separados.
3. Adjunta credenciales con un proveedor de sesión. Si existe refresh, serialízalo, limita reintentos y evita que el endpoint de renovación provoque recursión del autenticador.
4. Distingue errores HTTP, transporte y parsing. Respeta cancelación y no reintentes escrituras no idempotentes sin una estrategia del servidor.
5. Restringe logs y herramientas de inspección a desarrollo; elimina tokens, cuerpos y datos personales. Usa la política TLS de la plataforma, sin desactivar validaciones.
6. Si se solicita pinning, diseña rotación y recuperación antes de activarlo; no lo añadas automáticamente.

## Comprobar y entregar

Prueba respuestas válidas, 401, 429, 5xx, timeout y contenido inválido con un servidor controlado si el proyecto dispone de esa herramienta. Entrega configuración por ambiente y contrato de errores.
