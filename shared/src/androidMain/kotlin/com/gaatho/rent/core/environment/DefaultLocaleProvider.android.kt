package com.gaatho.rent.core.environment

import java.util.Locale

actual fun getDefaultLocale(): String {
    return Locale.getDefault().toString()
}
