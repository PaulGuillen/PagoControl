---
name: android-modularization
description: Diseñar o refactorizar módulos Android con límites de responsabilidad y dependencias sin ciclos. Usar al dividir features y capas compartidas.
---

# Diseñar módulos

## Dependencias de partida

```text
app -> features, core-data, navigation, core-platform, core-designsystem
features -> core-domain, session, navigation, core-designsystem, core-platform
core-data -> core-domain, session
core-domain -> Kotlin y librerías independientes de Android
session -> Kotlin y contratos mínimos
navigation -> contratos de rutas y serialización
core-designsystem -> Compose y recursos visuales
core-platform -> APIs Android y contratos técnicos necesarios
```

## Procedimiento

1. Asigna a cada módulo su API pública, propietario y dependencias permitidas. La UI conoce contratos; `app` compone implementaciones.
2. Separa paquetes para una feature pequeña; extrae módulos de dominio/data si se necesita imponer aislamiento de compilación o reutilización. No generes tres módulos vacíos por pantalla.
3. Evita dependencias entre features. Si un catálogo/renderizador es compartido, extrae una API coherente o un módulo explícito de capacidad compartida.
4. Coloca modelos compartidos donde corresponda su semántica; no conviertas `core-platform` en almacén de cualquier clase.

## Comprobar y entregar

Entrega diagrama de dependencias y reglas de ubicación. Compila consumidores afectados y verifica ausencia de ciclos y de imports Android en módulos puros. Usa `implementation` por defecto y `api` solo cuando el contrato expuesto lo requiera.
