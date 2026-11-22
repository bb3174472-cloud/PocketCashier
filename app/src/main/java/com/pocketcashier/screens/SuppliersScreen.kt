package com.pocketcashier.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Supplier
import com.pocketcashier.data.SupplierDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuppliersScreen(dao: SupplierDao) {
    val scope = rememberCoroutineScope()
    val suppliers by dao.getAllSuppliers().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("سجل الموردين") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مورد")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(suppliers) { supplier ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { selectedSupplier = supplier },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(supplier.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (supplier.phone.isNotBlank()) {
                                Text(supplier.phone, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text(
                            text = "${supplier.balance} ريال",
                            color = if (supplier.balance > 0) Color(0xFFF44336) else Color(0xFF4CAF50),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var initialBalance by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("إضافة مورد جديد") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المورد") })
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف (اختياري)") })
                    OutlinedTextField(
                        value = initialBalance, 
                        onValueChange = { initialBalance = it }, 
                        label = { Text("رصيد سابق (اختياري)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            dao.insert(Supplier(name = name, phone = phone, balance = initialBalance.toDoubleOrNull() ?: 0.0))
                            showAddDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }

    if (selectedSupplier != null) {
        val supplier = selectedSupplier!!
        var amount by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { selectedSupplier = null },
            title = { Text("حساب المورد: ${supplier.name}") },
            text = {
                Column {
                    Text("الرصيد المستحق: ${supplier.balance} ريال", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount, 
                        onValueChange = { amount = it }, 
                        label = { Text("المبلغ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val valAmount = amount.toDoubleOrNull() ?: 0.0
                                if (valAmount > 0) {
                                    scope.launch {
                                        dao.updateBalance(supplier.id, valAmount) // زيادة مستحقات المورد
                                        selectedSupplier = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) { Text("إضافة مستحقات") }

                        Button(
                            onClick = {
                                val valAmount = amount.toDoubleOrNull() ?: 0.0
                                if (valAmount > 0) {
                                    scope.launch {
                                        dao.updateBalance(supplier.id, -valAmount) // سداد للمورد
                                        selectedSupplier = null
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("سداد دفعة") }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSupplier = null }) { Text("إغلاق") }
            }
        )
    }
}
