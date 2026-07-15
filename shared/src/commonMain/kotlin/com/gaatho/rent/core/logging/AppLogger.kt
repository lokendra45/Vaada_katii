package com.gaatho.rent.core.logging

import co.touchlab.kermit.Logger

/**
 * Unified Touchlab Kermit Logger for Rent Manager Nepal (`co.touchlab:kermit`).
 *
 * Provides specialized tagged loggers for different architectural layers (`Network`, `Auth`, `UI`, `Database`)
 * while ensuring all logs flow through a single, cohesive logging pipeline across Android (Logcat) and iOS (OSLog).
 */
object AppLogger {
    val default = Logger.withTag("RentManager")
    val network = Logger.withTag("RentManager-Network")
    val auth = Logger.withTag("RentManager-Auth")
    val database = Logger.withTag("RentManager-DB")
    val ui = Logger.withTag("RentManager-UI")
}
