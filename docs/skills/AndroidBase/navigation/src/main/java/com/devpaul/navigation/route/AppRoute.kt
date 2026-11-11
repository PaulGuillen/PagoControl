package com.devpaul.navigation.route

/**
 * Contrato raíz de todas las rutas de la aplicación.
 *
 * Es sellado para mantener el catálogo de navegación dentro de este módulo. De ese modo,
 * cualquier feature consume rutas conocidas sin declarar identificadores incompatibles.
 */
sealed interface AppRoute

/**
 * Identifica una ruta contenedora creada con `navigation<T>()`.
 *
 * Separarla de los destinos permite aplicar en el futuro reglas de back stack a grafos
 * completos sin confundirlos con una pantalla Compose.
 */
sealed interface GraphRoute : AppRoute

/**
 * Identifica una ruta concreta creada con `composable<T>()`.
 *
 * Este contrato agrupa destinos sin imponer si usan `data object` o una `data class`
 * serializable con argumentos.
 */
sealed interface DestinationRoute : AppRoute
