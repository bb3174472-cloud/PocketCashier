package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Product
import com.pocketcashier.data.ProductDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(dao: ProductDao) {
    val scope = rememberCoroutineScope()
    val products by dao.getAllProducts().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("إدارة المنتجات") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منتج")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(products) { product ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.titleMedium)
                            Text("بيع: ${product.price} | شراء: ${product.cost} | الكمية: ${product.quantity}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { scope.launch { dao.delete(product) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var price by remember { mutableStateOf("") }
        var cost by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("منتج جديد") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") })
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("سعر البيع") })
                    OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("سعر التكلفة (الشراء)") })
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            dao.insert(Product(
                                name = name, 
                                price = price.toDoubleOrNull() ?: 0.0, 
                                cost = cost.toDoubleOrNull() ?: 0.0,
                                quantity = quantity.toIntOrNull() ?: 0
                            ))
                            showAddDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
