package org.com.bayarair.data.local

import android.content.Context

private const val PREFS = "settings"
private const val KEY_PRINTER_MAC = "printer_mac"

fun loadSavedPrinterMac(context: Context): String? =
    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .getString(KEY_PRINTER_MAC, null)

fun savePrinterMac(
    context: Context,
    mac: String,
) {
    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(KEY_PRINTER_MAC, mac)
        .apply()
}

fun clearPrinterMac(context: Context) {
    context
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        .edit()
        .remove(KEY_PRINTER_MAC)
        .apply()
}
