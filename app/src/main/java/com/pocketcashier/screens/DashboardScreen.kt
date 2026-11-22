package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.ProductDao
import com.pocketcashier.data.SaleDao
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(productDao: ProductDao, saleDao: SaleDao, onViewSalesClick: () -> Unit) {
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    
    // حساب بداية اليوم الحالي
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val totalSales by saleDao.getTotalSalesAmount().collectAsState(initial = 0.0)
    val todaySales by saleDao.getTodaySalesAmount(startOfDay).collectAsState(initial = 0.0)
    
    val lowStockCount = products.count { it.quantity < 5 }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("لوحة التحكم", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onViewSalesClick) {
                Icon(Icons.Default.List, contentDescription = "سجل المبيعات", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        // البطاقات المالية
        SummaryCard("مبيعات اليوم", "${todaySales ?: 0.0} ريال", Color(0xFF4CAF50), Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        SummaryCard("إجمالي المبيعات", "${totalSales ?: 0.0} ريال", Color(0xFF2196F3), Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        
        // بطاقة التنبيهات
        SummaryCard("منتجات قاربت على النفاد", lowStockCount.toString(), Color(0xFFF44336), Modifier.fillMaxWidth())
    }
}

@Composable
fun SummaryCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
