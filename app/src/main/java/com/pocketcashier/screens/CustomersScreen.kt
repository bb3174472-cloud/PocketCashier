package com.pocketcashier.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Customer
import com.pocketcashier.data.CustomerDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(dao: CustomerDao) {
    val scope = rememberCoroutineScope()
    val customers by dao.getAllCustomers().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("سجل العملاء والديون") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة عميل")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(customers) { customer ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedCustomer = customer },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (customer.phone.isNotBlank()) {
                                Text(customer.phone, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = "${customer.balance} ريال",
                            color = if (customer.balance > 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // نافذة إضافة عميل جديد
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var initialBalance by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة عميل جديد") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم العميل") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف (اختياري)") })
                    OutlinedTextField(value = initialBalance, onValueChange = { initialBalance = it }, label = { Text("دين سابق (اختياري)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            dao.insert(Customer(name = name, phone = phone, balance = initialBalance.toDoubleOrNull() ?: 0.0))
                            showAddDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }

    // نافذة إدارة رصيد العميل (دين أو سداد)
    selectedCustomer?.let { customer ->
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { selectedCustomer = null },
            title = { Text("حساب: ${customer.name}") },
            text = {
                Column {
                    Text("الرصيد الحالي: ${customer.balance} ريال", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val valAmount = amount.toDoubleOrNull() ?: 0.0
                                if (valAmount > 0) {
                                    scope.launch {
                                        dao.updateBalance(customer.id, valAmount) // زيادة الدين
                                        selectedCustomer = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) { Text("إضافة دين") }

                        Button(
                            onClick = {
                                val valAmount = amount.toDoubleOrNull() ?: 0.0
                                if (valAmount > 0) {
                                    scope.launch {
                                        dao.updateBalance(customer.id, -valAmount) // سداد (خصم)
                                        selectedCustomer = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("سداد دفعة") }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
