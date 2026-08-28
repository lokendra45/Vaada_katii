package com.gaatho.rent.core.utils

import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

actual suspend fun generateTenantPdf(
    tenantName: String,
    profileInfo: String,
    rentInfo: String
): ByteArray = withContext(Dispatchers.IO) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size roughly
    val page = document.startPage(pageInfo)
    
    val canvas = page.canvas
    val paint = Paint().apply {
        textSize = 14f
        isAntiAlias = true
    }
    val titlePaint = Paint().apply {
        textSize = 20f
        isFakeBoldText = true
        isAntiAlias = true
    }
    
    var yPosition = 50f
    
    // Draw Title
    canvas.drawText("Tenant Backup: $tenantName", 50f, yPosition, titlePaint)
    yPosition += 40f
    
    // Draw Profile Info
    canvas.drawText("Profile Information:", 50f, yPosition, titlePaint.apply { textSize = 16f })
    yPosition += 25f
    
    profileInfo.split("\n").forEach { line ->
        canvas.drawText(line, 50f, yPosition, paint)
        yPosition += 20f
    }
    
    yPosition += 20f
    
    // Draw Rent Info
    canvas.drawText("Rent Information:", 50f, yPosition, titlePaint)
    yPosition += 25f
    
    rentInfo.split("\n").forEach { line ->
        canvas.drawText(line, 50f, yPosition, paint)
        yPosition += 20f
    }
    
    document.finishPage(page)
    
    val outputStream = ByteArrayOutputStream()
    document.writeTo(outputStream)
    document.close()
    
    outputStream.toByteArray()
}
