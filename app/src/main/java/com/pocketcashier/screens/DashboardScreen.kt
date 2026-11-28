package com.pocketcashier.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.pocketcashier.AppNavy
import com.pocketcashier.data.ProductDao
import com.pocketcashier.data.SaleDao
import com.pocketcashier.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(productDao: ProductDao, saleDao: SaleDao, onViewSalesClick: () -> Unit) {
    val context = LocalContext.current
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    val totalSales by saleDao.getTotalSalesAmount().collectAsState(initial = 0.0)
    val lowStockCount = products.count { it.quantity < 5 }

    // طلب إذن الإشعارات
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    
    // حالة لحفظ بيانات الرسم البياني
    var chartEntries by remember { mutableStateOf<List<BarEntry>>(emptyList()) }
    var chartLabels by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        // 1. طلب إذن الإشعارات إذا كنا على أندرويد 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // 2. فحص الإشعارات إذا كانت المنتجات قد تم تحميلها
        if (products.isNotEmpty()) {
            NotificationHelper.checkAndNotifyLowStock(context, products)
        }

        // 3. جلب بيانات الرسم البياني
        withContext(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startDate = calendar.timeInMillis

            val sales = saleDao.getSalesSince(startDate)
            val dailySales = mutableMapOf<String, Double>()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            sales.forEach { sale ->
                val dateStr = dateFormat.format(Date(sale.timestamp))
                dailySales[dateStr] = (dailySales[dateStr] ?: 0.0) + sale.totalAmount
            }

            val entries = mutableListOf<BarEntry>()
            val labels = mutableListOf<String>()
            val displayFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -6)

            for (i in 0..6) {
                val dateStr = dateFormat.format(calendar.time)
                val labelStr = displayFormat.format(calendar.time)
                val amount = dailySales[dateStr] ?: 0.0
                entries.add(BarEntry(i.toFloat(), amount.toFloat()))
                labels.add(labelStr)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            chartEntries = entries
            chartLabels = labels
        }
    }

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
            Text("مبيعات آخر 7 أيام", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(modifier = Modifier.fillMaxWidth().height(250.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                if (chartEntries.isNotEmpty()) {
                    AndroidView(
                        factory = { ctx ->
                            BarChart(ctx).apply {
                                description.isEnabled = false
                                setDrawGridBackground(false)
                                axisRight.isEnabled = false
                                
                                xAxis.valueFormatter = IndexAxisValueFormatter(chartLabels)
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                xAxis.granularity = 1f
                                xAxis.textSize = 10f

                                val dataSet = BarDataSet(chartEntries, "المبيعات (ريال)")
                                dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
                                dataSet.valueTextColor = Color.Black.toArgb()
                                dataSet.valueTextSize = 9f
                                
                                data = BarData(dataSet)
                                data.barWidth = 0.5f
                                invalidate()
                            }
                        },
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
