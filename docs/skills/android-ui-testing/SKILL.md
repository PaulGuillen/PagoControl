---
name: android-ui-testing
description: Validar interfaces Compose, navegación y APIs Android con pruebas instrumentadas. Usar para comprobar flujos de usuario y regresiones dependientes del dispositivo.
---

# Probar UI y plataforma

## Procedimiento

1. Elige recorridos críticos y resultados observables: completar formulario, abrir detalle, volver y cerrar sesión sin recuperar datos anteriores.
2. Inyecta repositorios y reporters controlados para pruebas de UI deterministas. Reserva backend real para un conjunto de integración identificado.
3. Usa semántica y etiquetas accesibles para interactuar; añade tags estables cuando no haya identificadores semánticos suficientes.
4. Sincroniza operaciones mediante herramientas de prueba y condiciones observables. Evita tiempos de espera fijos que vuelvan frágil el flujo.
5. Incluye recreación, teclado, permiso denegado y tamaños/idiomas relevantes. Verifica que collectors y efectos no repitan acciones.
6. Prueba APIs Android como notificaciones y archivos con entornos aislados, limpieza de datos y permisos controlados.

## Comprobar y entregar

Ejecuta la tarea `connected<Variante>AndroidTest` existente en un dispositivo identificado. Conserva reportes y condiciones del entorno. Si no hay emulador/dispositivo, entrega las pruebas y declara que su ejecución instrumentada queda pendiente.
