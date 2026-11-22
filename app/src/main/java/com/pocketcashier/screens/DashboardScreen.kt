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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(dao: ProductDao, onViewSalesClick: () -> Unit) {
    val products by dao.getAllProducts().collectAsState(initial = emptyList())
    val totalProducts = products.size
    val totalStock = products.sumOf { it.quantity }
    val lowStockCount = products.count { it.quantity < 5 }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("لوحة التحكم", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onViewSalesClick) {
                Icon(Icons.Default.List, contentDescription = "سجل المبيعات", tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            SummaryCard("إجمالي المنتجات", totalProducts.toString(), Color(0xFF2196F3), Modifier.weight(1f))
            Spacer(modifier = Modifier.width(8.dp))
            SummaryCard("إجمالي القطع", totalStock.toString(), Color(0xFF4CAF50), Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(16.dp))
        
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
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineLarge, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
