---
name: android-navigation
description: Implementar navegación tipada con Navigation Compose, grafos por feature y restauración de back stack. Usar al conectar pantallas, sesión y enlaces de entrada.
---

# Componer grafos y rutas

## Procedimiento

1. Define contratos serializables de grafos y destinos en `navigation`. Usa IDs y parámetros pequeños; evita documentos completos, credenciales y objetos de SDK.
2. Cada feature implementa su grafo. `app/MainGraph` registra grafos y conecta callbacks entre funcionalidades.
3. Recupera argumentos mediante las APIs tipadas compatibles, como `toRoute`. Recarga los datos desde repositorios usando el identificador.
4. Define destinos iniciales, back, `popUpTo`, `launchSingleTop` y restauración para cada flujo. Tras login retira el flujo de acceso del historial.
5. Al cerrar sesión, limpia datos compartidos y estados guardados sensibles antes de permitir otra cuenta. Restaurar una pestaña no debe recuperar contenido del usuario anterior.
6. Asigna scope al ViewModel de un flujo compartido. Mantén autosave y reglas fuera del registro de rutas.
7. Valida entradas externas, espera sesión cuando sea necesaria y consume cada solicitud de navegación con una política explícita.

## Comprobar y entregar

Verifica login → inicio → detalle → atrás, cambio de pestaña, logout, recreación y entrada desde notificación. Entrega mapa de grafos y política de restauración. No migres de familia de navegación sin que el proyecto lo requiera.
