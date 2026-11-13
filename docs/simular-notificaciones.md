# Simular movimientos desde PowerShell

Requiere la app debug instalada, ADB conectado y **Monitorear Gmail** activado.
Desde la carpeta del proyecto:

```powershell
.\scripts\Simulate-Payment.ps1 -Scenario bcp -Amount 12.34
.\scripts\Simulate-Payment.ps1 -Scenario steam -Amount 12.34
.\scripts\Simulate-Payment.ps1 -Scenario pair -Amount 27.89
.\scripts\Simulate-Payment.ps1 -Scenario income -Amount 100.00
.\scripts\Simulate-Payment.ps1 -Scenario rejected -Amount 15.00
.\scripts\Simulate-Payment.ps1 -Scenario clear
```

- `bcp`: consumo bancario automatico, salvo posible duplicado.
- `steam`: recibo pendiente de confirmar.
- `pair`: BCP automatico y recibo Steam pendiente marcado como posible duplicado.
- `income`: ingreso Interbank automatico, salvo posible duplicado.
- `rejected`: operacion rechazada que no debe registrarse.
- `clear`: retira del balance y de pendientes los datos creados por el simulador, incluso si fueron confirmados. Conserva los movimientos reales.

Los ejemplos llevan fuente `Gmail (prueba)` e identificadores `debug-sim-`. Se mezclan temporalmente con el balance y el grafico: ejecuta `clear` al terminar. Cada ejecucion crea avisos nuevos; repetir el mismo importe en menos de 24 horas puede activar la regla de duplicados. Se usa la fecha actual del celular.

Esto inyecta ejemplos en el mismo repositorio que usa el capturador: prueba reconocimiento, guardado, duplicados, UI y graficos. No envia correos, push de Gmail ni operaciones bancarias, y no prueba la lectura de notificaciones de Android. El receptor solo existe en debug, nunca en release.

Con varios dispositivos, agrega `-Serial <identificador-adb>`.
