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
    onPurchasesClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onReturnsClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("العمليات الإضافية") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            MoreItem("المشتريات (شراء بضاعة)", Icons.Default.ShoppingBag, onPurchasesClick)
            MoreItem("المصروفات (إيجار، كهرباء..)", Icons.Default.Money, onExpensesClick)
            MoreItem("المرتجعات والتالف", Icons.Default.Refresh, onReturnsClick)
        }
    }
}

@Composable
fun MoreItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    Divider()
}
