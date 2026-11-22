package com.pocketcashier.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Sale
import com.pocketcashier.data.SaleDao
import com.pocketcashier.data.SaleItem
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(dao: SaleDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val sales by dao.getAllSales().collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المبيعات") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(sales) { sale ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { 
                            selectedSale = sale
                            scope.launch { saleItems = dao.getSaleItems(sale.id) }
                        }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("فاتورة رقم: ${sale.id}", fontWeight = FontWeight.Bold)
                            Text(dateFormat.format(Date(sale.timestamp)), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("الإجمالي: ${sale.totalAmount} ريال", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // نافذة تفاصيل الفاتورة
    if (selectedSale != null) {
        AlertDialog(
            onDismissRequest = { selectedSale = null },
            title = { Text("تفاصيل الفاتورة رقم ${selectedSale!!.id}") },
            text = {
                LazyColumn {
                    items(saleItems) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.productName)
                            Text("${item.quantity} × ${item.price} = ${item.quantity * item.price}")
                        }
                        Divider()
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSale = null }) { Text("إغلاق") }
            }
        )
    }
}
