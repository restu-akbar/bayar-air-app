package org.com.bayarair.utils

fun formatRupiah(value: Long): String = "Rp " + groupThousands(value.toString())

fun groupThousands(digits: String): String {
    if (digits.isEmpty()) return ""
    val sb = StringBuilder()
    var c = 0
    for (i in digits.length - 1 downTo 0) {
        sb.append(digits[i]); c++
        if (c == 3 && i != 0) {
            sb.append('.'); c = 0
        }
    }
    return sb.reverse().toString()
}
