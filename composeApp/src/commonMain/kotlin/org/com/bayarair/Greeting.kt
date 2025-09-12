package org.com.bayarair

import org.com.bayarair.platform.getPlatform

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = "Hello, ${platform.name}!"
}

