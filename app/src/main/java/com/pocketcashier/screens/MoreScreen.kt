package com.pocketcashier.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    isManager: Boolean,
    onPurchasesClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onReturnsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCashSessionClick: () -> Unit,
    onStocktakeClick: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("العمليات الإضافية") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            MoreItem("إدارة الصندوق (فتح/إغلاق)", Icons.Default.PointOfSale, onCashSessionClick)
            MoreItem("الجرد الفعلي", Icons.Default.Inventory, onStocktakeClick)
            if (isManager) {
                MoreItem("التقارير المالية (الأرباح)", Icons.Default.Assessment, onReportsClick)
            }
            MoreItem("المشتريات (شراء بضاعة)", Icons.Default.ShoppingBag, onPurchasesClick)
            MoreItem("المصروفات", Icons.Default.Money, onExpensesClick)
            MoreItem("المرتجعات والتالف", Icons.Default.Refresh, onReturnsClick)
            if (isManager) {
                MoreItem("الإعدادات والتحكم", Icons.Default.Settings, onSettingsClick)
            }
        }
    }
}

@Composable
fun MoreItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    Divider()
}
