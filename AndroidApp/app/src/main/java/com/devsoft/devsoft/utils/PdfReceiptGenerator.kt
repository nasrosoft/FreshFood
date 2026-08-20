package com.devsoft.devsoft.utils

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.devsoft.devsoft.domain.model.DeliveryOrderWithDetails
import com.devsoft.devsoft.presentation.sales.CartItem
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double = quantity * unitPrice
)

object PdfReceiptGenerator {

    private const val PREFS_NAME = "devsoft_brand_prefs"
    private const val KEY_BRAND_NAME = "store_brand_name"

    fun persistStoreName(context: Context, name: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BRAND_NAME, name).apply()
    }

    fun getPersistedStoreName(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BRAND_NAME, "FRESH DAIRY - FRESHFOOD") ?: "FRESH DAIRY - FRESHFOOD"
    }

    fun generateReceipt(
        context: Context,
        storeName: String? = null,
        orderId: String? = null,
        date: Date = Date(),
        customerName: String? = null,
        paymentMethod: String? = null,
        driverName: String? = null,
        orderStatus: String? = null,
        items: List<ReceiptItem>,
        totalAmount: Double,
        customerCurrentCredit: Double? = null,
        customerCreditLimit: Double? = null,
        isCreditSale: Boolean = false
    ): android.net.Uri? {
        val lang = LocaleHelper.getPersistedLanguage(context)
        val pdfDocument = PdfDocument()
        
        val hasCredit = customerCurrentCredit != null && (customerCurrentCredit > 0 || isCreditSale)
        val calculatedHeight = 280 + (items.size * 18) + (if (hasCredit) 90 else 0) + (if (!driverName.isNullOrBlank()) 16 else 0) + (if (!orderStatus.isNullOrBlank()) 16 else 0)
        val pageHeight = calculatedHeight.coerceAtLeast(520)
        
        val pageInfo = PdfDocument.PageInfo.Builder(300, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
        }
        
        var yPosition = 30f
        val xMargin = 14f
        val rightEdge = pageInfo.pageWidth - xMargin
        val centerX = pageInfo.pageWidth / 2f
        
        // 1. Header: Store Name from Supabase
        val finalStoreName = (storeName?.takeIf { it.isNotBlank() } ?: getPersistedStoreName(context)).uppercase()
        paint.textSize = 14f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(finalStoreName, centerX, yPosition, paint)
        
        // 2. Order ID & Date
        yPosition += 16f
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(date)
        val orderPrefix = when (lang) {
            "ar" -> "طلب : #"
            "fr" -> "Commande : #"
            else -> "Order: #"
        }
        val headerSubtitle = if (!orderId.isNullOrBlank()) {
            "$orderPrefix${orderId.take(8).uppercase()}   •   $dateString"
        } else {
            dateString
        }
        canvas.drawText(headerSubtitle, centerX, yPosition, paint)
        
        // Divider
        yPosition += 14f
        paint.strokeWidth = 1f
        canvas.drawLine(xMargin, yPosition, rightEdge, yPosition, paint)
        
        // 3. Body Header: Customer, Payment, Driver
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 10f
        val labelOffset = when (lang) {
            "ar" -> 85f
            "fr" -> 68f
            else -> 64f
        }

        val custLabel = when (lang) {
            "ar" -> "الزبون / Client:"
            "fr" -> "Client :"
            else -> "Customer:"
        }
        val guestLabel = when (lang) {
            "ar" -> "زبون عادي (Guest)"
            "fr" -> "Client comptoir"
            else -> "Guest"
        }
        val payLabel = when (lang) {
            "ar" -> "الدفع / Paiement:"
            "fr" -> "Paiement :"
            else -> "Payment:"
        }
        val driverLabel = when (lang) {
            "ar" -> "السائق / Livreur:"
            "fr" -> "Livreur :"
            else -> "Driver:"
        }
        val statusLabel = when (lang) {
            "ar" -> "الحالة / Statut:"
            "fr" -> "Statut :"
            else -> "Status:"
        }

        val rawMethod = paymentMethod?.uppercase() ?: "CASH"
        val translatedPaymentMethod = when {
            rawMethod.contains("CREDIT") -> when (lang) {
                "ar" -> "دين (CRÉDIT)"
                "fr" -> "CRÉDIT"
                else -> "CREDIT"
            }
            rawMethod.contains("BANK") || rawMethod.contains("CARD") -> when (lang) {
                "ar" -> "بطاقة / بنك"
                "fr" -> "CARTE / BANQUE"
                else -> "BANK / CARD"
            }
            else -> when (lang) {
                "ar" -> "نقداً (ESPÈCES)"
                "fr" -> "ESPÈCES"
                else -> "CASH"
            }
        }

        yPosition += 16f
        paint.isFakeBoldText = true
        canvas.drawText(custLabel, xMargin, yPosition, paint)
        paint.isFakeBoldText = false
        canvas.drawText(customerName?.ifBlank { guestLabel } ?: guestLabel, xMargin + labelOffset, yPosition, paint)

        yPosition += 14f
        paint.isFakeBoldText = true
        canvas.drawText(payLabel, xMargin, yPosition, paint)
        paint.isFakeBoldText = false
        canvas.drawText(translatedPaymentMethod, xMargin + labelOffset, yPosition, paint)

        if (!driverName.isNullOrBlank()) {
            yPosition += 14f
            paint.isFakeBoldText = true
            canvas.drawText(driverLabel, xMargin, yPosition, paint)
            paint.isFakeBoldText = false
            canvas.drawText(driverName, xMargin + labelOffset, yPosition, paint)
        }

        if (!orderStatus.isNullOrBlank()) {
            val translatedStatus = when (orderStatus) {
                "DELIVERED" -> if (lang == "ar") "مُسلّم" else if (lang == "fr") "Livré" else "Delivered"
                "OUT_FOR_DELIVERY" -> if (lang == "ar") "قيد التوصيل" else if (lang == "fr") "En cours de livraison" else "Out for Delivery"
                "ASSIGNED" -> if (lang == "ar") "مُعيّن" else if (lang == "fr") "Assigné" else "Assigned"
                "PENDING" -> if (lang == "ar") "في الانتظار" else if (lang == "fr") "En attente" else "Pending"
                else -> orderStatus
            }
            yPosition += 14f
            paint.isFakeBoldText = true
            canvas.drawText(statusLabel, xMargin, yPosition, paint)
            paint.isFakeBoldText = false
            canvas.drawText(translatedStatus, xMargin + labelOffset, yPosition, paint)
        }

        // Divider
        yPosition += 12f
        canvas.drawLine(xMargin, yPosition, rightEdge, yPosition, paint)

        // 4. Items Table Header
        val itemHeader = when (lang) {
            "ar" -> "المنتج / Article"
            "fr" -> "Article"
            else -> "Item"
        }
        val qtyPriceHeader = when (lang) {
            "ar" -> "الكمية x السعر"
            "fr" -> "Qté x Prix"
            else -> "Qty x Price"
        }
        val totalHeader = when (lang) {
            "ar" -> "المجموع"
            "fr" -> "Total"
            else -> "Total"
        }

        yPosition += 15f
        paint.textSize = 10.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(itemHeader, xMargin, yPosition, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(qtyPriceHeader, 215f, yPosition, paint)
        canvas.drawText(totalHeader, rightEdge, yPosition, paint)
        
        yPosition += 5f
        paint.isFakeBoldText = false
        canvas.drawLine(xMargin, yPosition, rightEdge, yPosition, paint)

        // 5. Items Rows
        paint.textSize = 10f
        for (item in items) {
            yPosition += 15f
            paint.textAlign = Paint.Align.LEFT
            val itemName = if (item.name.length > 17) item.name.substring(0, 14) + "..." else item.name
            canvas.drawText(itemName, xMargin, yPosition, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            val qtyPrice = "${item.quantity} x ${String.format(Locale.US, "%.1f", item.unitPrice)}"
            canvas.drawText(qtyPrice, 215f, yPosition, paint)
            
            val lineTotal = "${String.format(Locale.US, "%.1f", item.totalPrice)} DA"
            canvas.drawText(lineTotal, rightEdge, yPosition, paint)
        }
        
        // Divider before TOTAL
        yPosition += 10f
        canvas.drawLine(xMargin, yPosition, rightEdge, yPosition, paint)
        
        // 6. TOTAL
        val totalLabel = when (lang) {
            "ar" -> "الإجمالي / TOTAL"
            "fr" -> "TOTAL"
            else -> "TOTAL"
        }
        yPosition += 18f
        paint.textSize = 13.5f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(totalLabel, xMargin, yPosition, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${String.format(Locale.US, "%,.1f", totalAmount)} DA", rightEdge, yPosition, paint)
        
        // 7. Credit Details Section (if client has credit or credit sale)
        if (hasCredit) {
            val prevCredit = customerCurrentCredit ?: 0.0
            val currentCreditAdded = if (isCreditSale) totalAmount else 0.0
            val newTotalCredit = prevCredit + currentCreditAdded

            yPosition += 14f
            paint.strokeWidth = 0.75f
            canvas.drawLine(xMargin, yPosition, rightEdge, yPosition, paint)

            val creditSectionTitle = when (lang) {
                "ar" -> "تفاصيل الدين / DÉTAILS DU CRÉDIT"
                "fr" -> "DÉTAILS DU CRÉDIT"
                else -> "CREDIT INFORMATION"
            }
            val prevCreditLabel = when (lang) {
                "ar" -> "الدين السابق / Solde Précédent:"
                "fr" -> "Crédit Précédent :"
                else -> "Previous Credit:"
            }
            val saleOnCreditLabel = when (lang) {
                "ar" -> "البيع بالدين / Vente à Crédit:"
                "fr" -> "Vente à Crédit :"
                else -> "Sale on Credit:"
            }
            val currentCreditLabel = when (lang) {
                "ar" -> "إجمالي الدين / Total Dû:"
                "fr" -> "Total Crédit Actuel :"
                else -> "Total Current Credit:"
            }
            val limitLabel = when (lang) {
                "ar" -> "سقف الدين / Plafond:"
                "fr" -> "Plafond de Crédit :"
                else -> "Credit Limit:"
            }

            yPosition += 14f
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(creditSectionTitle, centerX, yPosition, paint)

            yPosition += 13f
            paint.textSize = 9.5f
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = false
            canvas.drawText(prevCreditLabel, xMargin, yPosition, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${String.format(Locale.US, "%,.1f", prevCredit)} DA", rightEdge, yPosition, paint)

            if (isCreditSale) {
                yPosition += 12f
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText(saleOnCreditLabel, xMargin, yPosition, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("+ ${String.format(Locale.US, "%,.1f", currentCreditAdded)} DA", rightEdge, yPosition, paint)
            }

            yPosition += 13f
            paint.textAlign = Paint.Align.LEFT
            paint.isFakeBoldText = true
            canvas.drawText(currentCreditLabel, xMargin, yPosition, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${String.format(Locale.US, "%,.1f", newTotalCredit)} DA", rightEdge, yPosition, paint)

            if (customerCreditLimit != null && customerCreditLimit > 0) {
                yPosition += 12f
                paint.textAlign = Paint.Align.LEFT
                paint.isFakeBoldText = false
                canvas.drawText(limitLabel, xMargin, yPosition, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText("${String.format(Locale.US, "%,.1f", customerCreditLimit)} DA", rightEdge, yPosition, paint)
            }
        }

        // 8. Footer
        val footerText = when (lang) {
            "ar" -> "شكراً لتعاملكم معنا ونتشرف بزيارتكم!"
            "fr" -> "Merci pour votre confiance et à bientôt !"
            else -> "Thank you for your business!"
        }
        yPosition += 28f
        paint.textSize = 9.5f
        paint.isFakeBoldText = false
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(footerText, centerX, yPosition, paint)
        
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

    // Convenience adapter for CartItem list (POS Checkout)
    fun generateAndGetUri(
        context: Context, 
        cartItems: List<CartItem>, 
        totalAmount: Double,
        customerName: String? = null,
        driverName: String? = null,
        paymentMethod: String? = null,
        orderId: String? = null,
        storeName: String? = null,
        customerCurrentCredit: Double? = null,
        customerCreditLimit: Double? = null,
        orderStatus: String? = null
    ): android.net.Uri? {
        val receiptItems = cartItems.map {
            ReceiptItem(
                name = it.product.name,
                quantity = it.quantity,
                unitPrice = it.product.selling_price,
                totalPrice = it.quantity * it.product.selling_price
            )
        }
        val isCredit = paymentMethod?.equals("CREDIT", ignoreCase = true) == true
        return generateReceipt(
            context = context,
            storeName = storeName,
            orderId = orderId,
            customerName = customerName,
            paymentMethod = paymentMethod,
            driverName = driverName,
            orderStatus = orderStatus,
            items = receiptItems,
            totalAmount = totalAmount,
            customerCurrentCredit = customerCurrentCredit,
            customerCreditLimit = customerCreditLimit,
            isCreditSale = isCredit
        )
    }

    // Convenience adapter for DeliveryOrderWithDetails
    fun generateDeliveryReceipt(
        context: Context, 
        details: DeliveryOrderWithDetails, 
        paymentMethod: String,
        storeName: String? = null
    ): android.net.Uri? {
        val driverFullName = listOfNotNull(details.driver?.first_name, details.driver?.last_name)
            .joinToString(" ")
            .ifBlank { details.driver?.email }

        val receiptItems = details.items.map {
            val pName = it.product?.name ?: "Product"
            val price = it.product?.selling_price ?: 0.0
            ReceiptItem(
                name = pName,
                quantity = it.item.quantity,
                unitPrice = price,
                totalPrice = it.item.quantity * price
            )
        }
        val total = receiptItems.sumOf { it.totalPrice }
        val isCredit = paymentMethod.equals("CREDIT", ignoreCase = true)

        return generateReceipt(
            context = context,
            storeName = storeName,
            orderId = details.order.id,
            customerName = details.customer?.name ?: "Customer",
            paymentMethod = paymentMethod,
            driverName = driverFullName,
            orderStatus = details.order.status,
            items = receiptItems,
            totalAmount = total,
            customerCurrentCredit = details.customer?.current_credit,
            customerCreditLimit = details.customer?.credit_limit,
            isCreditSale = isCredit
        )
    }
}

