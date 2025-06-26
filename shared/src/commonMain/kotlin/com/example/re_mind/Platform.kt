package com.example.re_mind

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform