package com.example.sentinel_ip.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.sentinel_ip.data.model.CheckData
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateAndSharePdfReport(context: Context, data: CheckData) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.parseColor("#FF2B4A")
            textSize = 20f
            isFakeBoldText = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
        }

        val headerPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
            isFakeBoldText = true
        }

        var yPos = 40f

        canvas.drawText("Sentinel-IP Threat Intelligence Briefing", 40f, yPos, titlePaint)
        yPos += 30f

        canvas.drawText("Target IP Address: ${data.ipAddress}", 40f, yPos, headerPaint)
        yPos += 20f

        val level = when {
            data.abuseConfidenceScore < 25 -> "Safe / Low Risk"
            data.abuseConfidenceScore < 75 -> "Suspicious"
            else -> "Critical Threat / Malicious"
        }

        canvas.drawText("Abuse Confidence Score: ${data.abuseConfidenceScore}% ($level)", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("Country: ${data.countryName ?: "Unknown"} (${data.countryCode ?: "N/A"})", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("ISP: ${data.isp ?: "N/A"}", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("Domain: ${data.domain ?: "N/A"}", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("Usage Type: ${data.usageType ?: "Unspecified"}", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("Tor Exit Node: ${if (data.isTor == true) "YES" else "No"}", 40f, yPos, textPaint)
        yPos += 18f
        canvas.drawText("Total Reports: ${data.totalReports} (${data.numDistinctUsers} distinct reporters)", 40f, yPos, textPaint)
        yPos += 30f

        canvas.drawText("Generated via Sentinel-IP Android App", 40f, yPos, textPaint)

        document.finishPage(page)

        val file = File(context.cacheDir, "Sentinel_Report_${data.ipAddress.replace(".", "_")}.pdf")
        try {
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF Threat Briefing"))
        } catch (_: Exception) {
            document.close()
        }
    }
}
