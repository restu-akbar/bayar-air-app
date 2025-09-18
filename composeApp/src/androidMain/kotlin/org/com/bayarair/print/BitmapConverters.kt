package org.com.bayarair.print

import android.graphics.Bitmap

fun bitmapToEscPosRasterBytes(
    src: Bitmap,
    threshold: Int = 160,
): Pair<Int, ByteArray> {
    val targetW = 384
    val scale = targetW.toFloat() / src.width
    val targetH = (src.height * scale).toInt().coerceAtLeast(1)
    val bmp = Bitmap.createScaledBitmap(src, targetW, targetH, true)

    val widthBytes = (bmp.width + 7) / 8
    val out = ByteArray(widthBytes * bmp.height)

    var idx = 0
    val row = IntArray(bmp.width)
    for (y in 0 until bmp.height) {
        bmp.getPixels(row, 0, bmp.width, 0, y, bmp.width, 1)
        var bit = 0
        var byteVal = 0
        for (x in 0 until bmp.width) {
            val c = row[x]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val lum = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
            val black = lum < threshold
            byteVal = (byteVal shl 1) or (if (black) 1 else 0)
            bit++
            if (bit == 8) {
                out[idx++] = byteVal.toByte()
                bit = 0
                byteVal = 0
            }
        }
        if (bit != 0) {
            byteVal = byteVal shl (8 - bit)
            out[idx++] = byteVal.toByte()
        }
    }
    return widthBytes to out
}
