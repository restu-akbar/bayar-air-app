package org.com.bayarair.print

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun renderPdfToBitmaps(context: Context, pdfFile: File): List<Bitmap> =
    withContext(Dispatchers.IO) {
        val pfd = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(pfd)
        val bitmaps = mutableListOf<Bitmap>()
        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val scale = 384f / page.width
            val bmp = Bitmap.createBitmap(
                (page.width * scale).toInt().coerceAtLeast(384),
                (page.height * scale).toInt().coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bmp)
            canvas.drawColor(Color.WHITE)
            val matrix = Matrix().apply { setScale(scale, scale) }
            page.render(bmp, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_PRINT)
            page.close()
            bitmaps.add(bmp)
        }
        renderer.close()
        pfd.close()
        bitmaps
    }
