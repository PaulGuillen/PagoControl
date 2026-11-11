package com.devpaul.pagocontrol.features.movements.ui.movements

data class HistoryMovement(
    val id: String,
    val title: String,
    val detail: String,
    val source: String,
    val amountCents: Long,
    val day: Int,
    val badge: String,
    val status: String,
    val kind: String,
    val pending: Boolean = false,
)

internal fun demoMovements() = listOf(
    HistoryMovement("1", "María Rodríguez", "Ventas del Día • 10:42 AM", "Yape", 8500, 28, "Auto-detectado (Notif)", "Confirmado", "income"),
    HistoryMovement("2", "La Lucha Sanguchería", "Alimentación • 02:15 PM", "Plin", -3250, 28, "Plin BBVA", "Comprobante", "expense"),
    HistoryMovement("3", "Transferencia recibida", "BCP Notificaciones • 11:20 AM", "BCP", 12000, 28, "Pendiente de revisión", "Sin categoría", "income", true),
    HistoryMovement("4", "Consultoría TI & Cloud", "Servicios Profesionales • 04:50 PM", "BCP", 45000, 27, "Abono directo BCP", "Factura 001-49", "income"),
    HistoryMovement("5", "Netflix Perú Mensual", "Suscripción • Interbank Débito", "Interbank", -4490, 27, "Recurrente", "Automático", "expense"),
    HistoryMovement("6", "Cuenta Ahorros Propia", "Entre cuentas • BCP a Interbank", "BCP", 50000, 27, "Transferencia interna (Excluida)", "Mismo titular", "transfer"),
    HistoryMovement("7", "Bodega Don Lucho", "Venta mercadería • 07:15 PM", "Yape", 2400, 25, "Cobro Yape QR", "Caja Chica", "income"),
    HistoryMovement("8", "Recibo de Luz Pluz Energía", "Servicios básicos • Débito automático", "Interbank", -11580, 25, "Suministro #49821", "Pagado", "expense"),
)
