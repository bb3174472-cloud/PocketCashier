package com.pocketcashier.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.pocketcashier.R
import com.pocketcashier.data.Product

object NotificationHelper {
    private const val CHANNEL_ID = "low_stock_channel"

    fun checkAndNotifyLowStock(context: Context, products: List<Product>) {
        val lowStockProducts = products.filter { it.quantity < 5 }
        if (lowStockProducts.isEmpty()) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "تنبيهات المخزون",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "إشعارات للمنتجات القاربة على النفاد" }
            notificationManager.createNotificationChannel(channel)
        }

        val names = lowStockProducts.take(3).joinToString("، ") { it.name }
        val message = if (lowStockProducts.size > 3) "المنتجات: $names... والمزيد" else "المنتجات: $names"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ تنبيه: منتجات قاربت على النفاد")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}
