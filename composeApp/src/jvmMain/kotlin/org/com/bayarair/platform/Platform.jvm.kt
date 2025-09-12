package org.com.bayarair.platform

import org.com.bayarair.platform.Platform

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

