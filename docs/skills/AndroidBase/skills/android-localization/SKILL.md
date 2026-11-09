---
name: android-localization
description: Implementar recursos traducibles, formatos regionales y preferencias de idioma y apariencia en Android. Usar al añadir idiomas o personalización visual persistente.
---

# Gestionar idioma y apariencia

## Procedimiento

1. Extrae textos a recursos del módulo propietario; usa plurales y argumentos de formato. Evita concatenaciones que impidan reordenar una traducción.
2. Define idioma del sistema frente a idioma elegido y política para cambios en ejecución. Usa APIs de idiomas por aplicación compatibles con la base Android elegida.
3. Persiste idioma/apariencia mediante un adaptador. La preferencia y el tema visual tienen responsabilidades diferentes.
4. Da formato a fechas, números y moneda según locale explícito; no uses el texto visible como ID de negocio.
5. Conserva errores de dominio como códigos y traduce en presentación. Revisa textos en emails externos, documentos y notificaciones donde corresponda.
6. Contempla expansión de texto y RTL si está en alcance. Define fallback para idiomas no soportados y recursos ausentes.

## Comprobar y entregar

Prueba reinicio, cambio de idioma, modo claro/oscuro y configuración del sistema. Comprueba que navegación y formularios no pierdan datos indebidamente. Entrega recursos y política de selección/fallback.
