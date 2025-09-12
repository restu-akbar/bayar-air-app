package org.com.bayarair.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
