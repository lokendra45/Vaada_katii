package com.gaatho.rent

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform