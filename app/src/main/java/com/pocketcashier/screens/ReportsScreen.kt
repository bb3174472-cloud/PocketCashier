package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(saleDao: SaleDao, expenseDao: ExpenseDao, purchaseDao: PurchaseDao) {
    val totalSales by saleDao.getTotalSalesAmount().collectAsState(initial = 0.0)
    val totalExpenses by expenseDao.getTotalExpenses().collectAsState(initial = 0.0)
    val totalPurchases by purchaseDao.getTotalPurchases().collectAsState(initial = 0.0)

    // حساب صافي الربح التقديري (المبيعات - المصروفات - المشتريات)
    // ملاحظة: هذا حساب مبسط، الحساب الدقيق يحتاج لربط تكلفة كل منتج تم بيعه
    val estimatedProfit = (totalSales ?: 0.0) - (totalExpenses ?: 0.0) - (totalPurchases ?: 0.0)

    Scaffold(
        topBar = { TopAppBar(title = { Text("التقارير المالية") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            ReportCard("إجمالي المبيعات", "${totalSales ?: 0.0} ريال", Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(8.dp))
            
            ReportCard("إجمالي المشتريات (البضاعة)", "${totalPurchases ?: 0.0} ريال", Color(0xFF2196F3))
            Spacer(modifier = Modifier.height(8.dp))
            
            ReportCard("إجمالي المصروفات", "${totalExpenses ?: 0.0} ريال", Color(0xFFF44336))
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider(thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("صافي الربح التقديري", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${estimatedProfit} ريال",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (estimatedProfit >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(title: String, value: String, color: Color) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
