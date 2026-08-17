package com.devsoft.freshfood.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.devsoft.freshfood.presentation.sales.CartItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReceiptGenerator {
    
    fun generateAndGetUri(context: Context, cartItems: List<CartItem>, totalAmount: Double): android.net.Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        var yPosition = 30f
        val xMargin = 10f
        
        // Header
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("FRESHFOOD", pageInfo.pageWidth / 2f, yPosition, paint)
        
        yPosition += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        canvas.drawText(dateFormat.format(Date()), pageInfo.pageWidth / 2f, yPosition, paint)
        
        yPosition += 20f
        paint.strokeWidth = 1f
        canvas.drawLine(xMargin, yPosition, pageInfo.pageWidth - xMargin, yPosition, paint)
        
        // Items
        yPosition += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        for (item in cartItems) {
            val itemName = if (item.product.name.length > 20) item.product.name.substring(0, 17) + "..." else item.product.name
            canvas.drawText(itemName, xMargin, yPosition, paint)
            
            val qtyPrice = "${item.quantity} x ${item.product.selling_price} DA"
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(qtyPrice, pageInfo.pageWidth - xMargin, yPosition, paint)
            
            paint.textAlign = Paint.Align.LEFT
            yPosition += 15f
        }
        
        // Total
        yPosition += 10f
        canvas.drawLine(xMargin, yPosition, pageInfo.pageWidth - xMargin, yPosition, paint)
        
        yPosition += 20f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL", xMargin, yPosition, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$totalAmount DA", pageInfo.pageWidth - xMargin, yPosition, paint)
        
        // Footer
        yPosition += 40f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for your purchase!", pageInfo.pageWidth / 2f, yPosition, paint)
        
        pdfDocument.finishPage(page)
        
        return try {
            val file = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }

    fun generateDeliveryReceipt(context: Context, details: com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails, paymentMethod: String): android.net.Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        
        var yPosition = 30f
        val xMargin = 10f
        
        // Header
        paint.textSize = 16f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("FRESHFOOD - DELIVERY", pageInfo.pageWidth / 2f, yPosition, paint)
        
        yPosition += 20f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        canvas.drawText(dateFormat.format(Date()), pageInfo.pageWidth / 2f, yPosition, paint)
        
        yPosition += 20f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("Order ID: ${details.order.id.take(8)}", xMargin, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Customer: ${details.customer?.name ?: "Unknown"}", xMargin, yPosition, paint)
        val driverFullName = listOfNotNull(details.driver?.first_name, details.driver?.last_name)
            .joinToString(" ")
            .ifBlank { details.driver?.email ?: "N/A" }
        yPosition += 15f
        canvas.drawText("Driver: $driverFullName", xMargin, yPosition, paint)
        yPosition += 15f
        canvas.drawText("Payment: $paymentMethod", xMargin, yPosition, paint)
        
        yPosition += 10f
        paint.strokeWidth = 1f
        canvas.drawLine(xMargin, yPosition, pageInfo.pageWidth - xMargin, yPosition, paint)
        
        // Items
        yPosition += 20f
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 12f
        var totalAmount = 0.0
        for (itemDetail in details.items) {
            val product = itemDetail.product
            val itemName = product?.name ?: "Unknown"
            val displayItemName = if (itemName.length > 20) itemName.substring(0, 17) + "..." else itemName
            canvas.drawText(displayItemName, xMargin, yPosition, paint)
            
            val price = product?.selling_price ?: 0.0
            totalAmount += (price * itemDetail.item.quantity)
            val qtyPrice = "${itemDetail.item.quantity} x $price DA"
            
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(qtyPrice, pageInfo.pageWidth - xMargin, yPosition, paint)
            
            paint.textAlign = Paint.Align.LEFT
            yPosition += 15f
        }
        
        // Total
        yPosition += 10f
        canvas.drawLine(xMargin, yPosition, pageInfo.pageWidth - xMargin, yPosition, paint)
        
        yPosition += 20f
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText("TOTAL", xMargin, yPosition, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("$totalAmount DA", pageInfo.pageWidth - xMargin, yPosition, paint)
        
        // Footer
        yPosition += 40f
        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Thank you for your purchase!", pageInfo.pageWidth / 2f, yPosition, paint)
        
        pdfDocument.finishPage(page)
        
        return try {
            val file = File(context.cacheDir, "delivery_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            pdfDocument.close()
            null
        }
    }
}
