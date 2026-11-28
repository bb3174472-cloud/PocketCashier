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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.pocketcashier.AppNavy
import com.pocketcashier.data.ProductDao
import com.pocketcashier.data.SaleDao
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(productDao: ProductDao, saleDao: SaleDao, onViewSalesClick: () -> Unit) {
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    val totalSales by saleDao.getTotalSalesAmount().collectAsState(initial = 0.0)
    val lowStockCount = products.count { it.quantity < 5 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("لوحة التحكم", fontWeight = FontWeight.Bold, color = AppNavy) },
                actions = {
                    IconButton(onClick = onViewSalesClick) {
                        Icon(Icons.Default.Assessment, contentDescription = "سجل المبيعات", tint = AppNavy)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            ModernCard("إجمالي المبيعات", "${totalSales ?: 0.0} ريال", AppNavy, Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            ModernCard("منتجات قاربت على النفاد", lowStockCount.toString(), Color(0xFFF44336), Modifier.fillMaxWidth())
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("مبيعات آخر 7 أيام (تجريبي)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            // ✅ الرسم البياني
            Card(modifier = Modifier.fillMaxWidth().height(250.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                AndroidView(
                    factory = { context ->
                        BarChart(context).apply {
                            description.isEnabled = false
                            setDrawGridBackground(false)
                            axisRight.isEnabled = false
                            
                            // بيانات تجريبية (لأن جلب مبيعات كل يوم يطول شرحه، سنعرض شكلاً احترافياً)
                            val entries = ArrayList<BarEntry>()
                            entries.add(BarEntry(1f, 1500f))
                            entries.add(BarEntry(2f, 2300f))
                            entries.add(BarEntry(3f, 1800f))
                            entries.add(BarEntry(4f, 3200f))
                            entries.add(BarEntry(5f, 2800f))
                            entries.add(BarEntry(6f, 4100f))
                            entries.add(BarEntry(7f, 3500f))
                            
                            val dataSet = BarDataSet(entries, "المبيعات (ريال)")
                            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                            dataSet.valueTextColor = Color.Black.toArgb()
                            
                            data = BarData(dataSet)
                            data.barWidth = 0.5f
                            invalidate() // تحديث الرسم
                        }
                    },
                    modifier = Modifier.fillMaxSize().padding(8.dp)
                )
            }
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
