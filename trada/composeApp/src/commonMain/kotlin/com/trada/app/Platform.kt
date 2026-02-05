package com.trada.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform