package org.com.bayarair.data.token

import platform.Foundation.NSUserDefaults

private const val KEY_TOKEN = "token"

class IosTokenStorage : TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun readToken(): String? = defaults.stringForKey(KEY_TOKEN)

    override fun writeToken(token: String?) {
        if (token == null) {
            defaults.removeObjectForKey(KEY_TOKEN)
        } else {
            defaults.setObject(token, forKey = KEY_TOKEN)
        }
    }
}