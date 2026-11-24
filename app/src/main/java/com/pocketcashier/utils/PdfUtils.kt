package com.pocketcashier.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.pocketcashier.data.Sale
import com.pocketcashier.data.SaleItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun generateInvoicePdf(
    context: Context,
    uri: Uri,
    sale: Sale,
    items: List<SaleItem>,
    storeName: String = "كاشير الجيب"
): Boolean {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create() // مقاس صغير يشبه فاتورة الكاشير
    val page = pdfDocument.startPage(pageInfo)
    val canvas: Canvas = page.canvas
    val paint = Paint()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    var y = 30f
    val centerX = 150f

    // اسم المتجر
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 18f
    paint.isFakeBoldText = true
    canvas.drawText(storeName, centerX, y, paint)
    
    // خط فاصل
    y += 10f
    paint.strokeWidth = 1f
    canvas.drawLine(20f, y, 280f, y, paint)

    // معلومات الفاتورة
    y += 20f
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 12f
    paint.isFakeBoldText = false
    canvas.drawText("فاتورة رقم: ${sale.id}", 280f, y, paint)
    y += 15f
    canvas.drawText("التاريخ: ${dateFormat.format(Date(sale.timestamp))}", 280f, y, paint)
    y += 20f

    // جدول المنتجات
    paint.textAlign = Paint.Align.LEFT
    canvas.drawText("الصنف", 20f, y, paint)
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText("الكمية", 150f, y, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("الإجمالي", 280f, y, paint)
    
    y += 10f
    canvas.drawLine(20f, y, 280f, y, paint)

    items.forEach { item ->
        y += 20f
        paint.textAlign = Paint.Align.LEFT
        // تقصير الاسم إذا كان طويلاً
        val name = if (item.productName.length > 15) item.productName.substring(0, 15) + "..." else item.productName
        canvas.drawText(name, 20f, y, paint)
        
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(item.quantity.toString(), 150f, y, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("${item.price * item.quantity}", 280f, y, paint)
    }

    // الإجمالي والخصم
    y += 15f
    canvas.drawLine(20f, y, 280f, y, paint)
    
    y += 20f
    paint.textAlign = Paint.Align.RIGHT
    paint.textSize = 14f
    paint.isFakeBoldText = true
    canvas.drawText("الإجمالي: ${sale.totalAmount} ريال", 280f, y, paint)
    
    if (sale.discount > 0) {
        y += 15f
        paint.textSize = 12f
        canvas.drawText("الخصم: ${sale.discount} ريال", 280f, y, paint)
    }
    
    if (sale.paymentType != "نقدي") {
        y += 15f
        paint.textSize = 12f
        canvas.drawText("المدفوع: ${sale.paidAmount} ريال", 280f, y, paint)
        y += 15f
        canvas.drawText("المتبقي (دين): ${sale.totalAmount - sale.paidAmount} ريال", 280f, y, paint)
    }

    y += 30f
    paint.textAlign = Paint.Align.CENTER
    paint.textSize = 12f
    canvas.drawText("شكراً لتسوقكم معنا!", centerX, y, paint)

    pdfDocument.finishPage(page)

    return try {
        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
            pdfDocument.writeTo(outputStream)
        }
        pdfDocument.close()
        true
    } catch (e: Exception) {
        e.printStackTrace()
        pdfDocument.close()
        false
    }
}
