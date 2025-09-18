package org.com.bayarair.print

object EscPos {
    val INIT = byteArrayOf(0x1B, 0x40)
    val LINE_FEED = byteArrayOf(0x0A)

    fun alignCenter() = byteArrayOf(0x1B, 0x61, 0x01)

    fun alignLeft() = byteArrayOf(0x1B, 0x61, 0x00)

    fun cutPartial() = byteArrayOf(0x1D, 0x56, 0x42, 0x00)

    fun rasterBitImage(
        widthBytes: Int,
        height: Int,
        data: ByteArray,
    ): ByteArray {
        val header =
            byteArrayOf(
                0x1D,
                0x76,
                0x30,
                0x00,
                (widthBytes and 0xFF).toByte(),
                ((widthBytes shr 8) and 0xFF).toByte(),
                (height and 0xFF).toByte(),
                ((height shr 8) and 0xFF).toByte(),
            )
        return header + data
    }
}
