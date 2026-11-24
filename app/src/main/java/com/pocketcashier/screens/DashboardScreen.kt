package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.AppGold
import com.pocketcashier.AppNavy
import com.pocketcashier.data.ProductDao
import com.pocketcashier.data.SaleDao
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(productDao: ProductDao, saleDao: SaleDao, onViewSalesClick: () -> Unit) {
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    
    val startOfDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val totalSales by saleDao.getTotalSalesAmount().collectAsState(initial = 0.0)
    val todaySales by saleDao.getTodaySalesAmount(startOfDay).collectAsState(initial = 0.0)
    
    val lowStockCount = products.count { it.quantity < 5 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم", fontWeight = FontWeight.Bold, color = AppNavy) },
                actions = {
                    IconButton(onClick = onViewSalesClick) {
                        Icon(Icons.Default.Assessment, contentDescription = "سجل المبيعات", tint = AppNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            // البطاقات المالية بتصميم عصري
            ModernCard("مبيعات اليوم", "${todaySales ?: 0.0} ريال", Color(0xFF4CAF50), Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            ModernCard("إجمالي المبيعات", "${totalSales ?: 0.0} ريال", AppNavy, Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            ModernCard("منتجات قاربت على النفاد", lowStockCount.toString(), Color(0xFFF44336), Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun ModernCard(title: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}
