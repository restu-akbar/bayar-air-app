package org.com.bayarair.print

import android.bluetooth.BluetoothManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.com.bayarair.utils.ReceiptPrinter
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit
import android.util.Log
import android.os.Build

private const val TAG = "ReceiptPrint"

class BluetoothEscPosPrinter(
    private val context: Context,
    private val printerMac: String,
) : ReceiptPrinter {
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override suspend fun printPdfFromUrl(url: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "printPdfFromUrl start | mac=$printerMac url=$url")

        // 1) Download PDF
        val pdfFile = downloadPdf(url)
        Log.d(TAG, "PDF downloaded | path=${pdfFile.absolutePath} size=${pdfFile.length()} bytes")

        // 2) Render PDF → Bitmaps
        val tRenderStart = System.currentTimeMillis()
        val pages = renderPdfToBitmaps(context, pdfFile)
        Log.d(TAG, "PDF rendered | pages=${pages.size} in ${System.currentTimeMillis() - tRenderStart} ms")

        // 3) Setup Bluetooth
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = btManager.adapter ?: error("Bluetooth tidak tersedia di perangkat")
        val device = adapter.getRemoteDevice(printerMac)
        val socket = device.createRfcommSocketToServiceRecord(sppUuid)

        try {
            if (Build.VERSION.SDK_INT < 31) {
                adapter.cancelDiscovery()
            } else {
                Log.d(TAG, "Skipping cancelDiscovery() on API >= 31 to avoid BLUETOOTH_SCAN requirement")
            }

            Log.d(TAG, "Connecting to printer: name=${device.name} address=${device.address}")
            socket.connect()

            BufferedOutputStream(socket.outputStream).use { os ->
                // 4) ESC/POS init
                os.write(EscPos.INIT)
                os.write(EscPos.alignCenter())
                Log.d(TAG, "ESC/POS init sent")

                pages.forEachIndexed { index, bmp ->
                    val (widthBytes, raster) = bitmapToEscPosRasterBytes(bmp)
                    Log.d(TAG, "Sending page ${index + 1}/${pages.size} | h=${bmp.height} widthBytes=$widthBytes rasterSize=${raster.size}")
                    os.write(EscPos.rasterBitImage(widthBytes, bmp.height, raster))
                    os.write(EscPos.LINE_FEED)
                    if (index < pages.lastIndex) os.write(EscPos.LINE_FEED)
                }

                repeat(3) { os.write(EscPos.LINE_FEED) }
                os.write(EscPos.cutPartial())
                os.flush()
                Log.i(TAG, "Print data sent successfully")
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Printing failed", t)
            throw t
        } finally {
            runCatching { socket.close() }.onFailure { Log.w(TAG, "Error closing socket", it) }
            Log.d(TAG, "printPdfFromUrl end")
        }
        Unit
    }

    private fun downloadPdf(url: String): File {
        Log.d(TAG, "Downloading PDF: $url")
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val res = client.newCall(Request.Builder().url(url).build()).execute()
        if (!res.isSuccessful) {
            val code = res.code
            val msg = res.message
            Log.e(TAG, "HTTP error: $code $msg")
            error("Gagal download PDF: $code $msg")
        }

        val body = res.body ?: error("Body kosong")
        val tmp = File.createTempFile("struk_", ".pdf", context.cacheDir)
        body.byteStream().use { input ->
            FileOutputStream(tmp).use { output -> input.copyTo(output) }
        }
        return tmp
    }
}
