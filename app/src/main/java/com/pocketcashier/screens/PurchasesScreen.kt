package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Purchase
import com.pocketcashier.data.PurchaseDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(dao: PurchaseDao) {
    val scope = rememberCoroutineScope()
    val purchases by dao.getAllPurchases().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("سجل المشتريات") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(purchases) { purchase ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(purchase.description, style = MaterialTheme.typography.titleMedium)
                        Text("المبلغ: ${purchase.amount} ريال", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    if (showDialog) {
        var desc by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("إضافة مشتريات") },
            text = {
                Column {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف (مثال: شراء أرز)") })
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (desc.isNotBlank()) {
                        scope.launch {
                            dao.insertPurchase(Purchase(description = desc, amount = amount.toDoubleOrNull() ?: 0.0))
                            showDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
