package org.com.bayarair

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform