package com.pocketcashier.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Product
import com.pocketcashier.data.ProductDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(dao: ProductDao) {
    val scope = rememberCoroutineScope()
    val products by dao.getAllProducts().collectAsState(initial = emptyList())
    
    // متغيرات فتح النوافذ
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clickable { editingProduct = product }, // الضغط للتعديل
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${product.name} (${product.unit})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("بيع: ${product.price} | شراء: ${product.cost}", style = MaterialTheme.typography.bodySmall)
                            Text("الكمية المتوفرة: ${product.quantity}", color = if (product.quantity < 5) Color.Red else Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            if (!product.expiryDate.isNullOrBlank()) {
                                Text("ينتهي في: ${product.expiryDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(onClick = { scope.launch { dao.delete(product) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // نافذة الإضافة أو التعديل
    if (showAddDialog || editingProduct != null) {
        val isEditing = editingProduct != null
        
        var name by remember { mutableStateOf(editingProduct?.name ?: "") }
        var price by remember { mutableStateOf(editingProduct?.price?.toString() ?: "") }
        var cost by remember { mutableStateOf(editingProduct?.cost?.toString() ?: "") }
        var barcode by remember { mutableStateOf(editingProduct?.barcode ?: "") }
        var unit by remember { mutableStateOf(editingProduct?.unit ?: "حبة") }
        var expiryDate by remember { mutableStateOf(editingProduct?.expiryDate ?: "") }
        var quantity by remember { mutableStateOf(editingProduct?.quantity?.toString() ?: "") }
        
        AlertDialog(
            onDismissRequest = { 
                showAddDialog = false
                editingProduct = null 
            },
            title = { Text(if (isEditing) "تعديل منتج" else "منتج جديد") },
            text = {
                LazyColumn {
                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("سعر البيع") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("سعر التكلفة") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("الباركود") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("الوحدة (حبة، كرتون..)") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("تاريخ الانتهاء (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            val newProduct = Product(
                                id = editingProduct?.id ?: 0, // إذا كان تعديلاً، نحتفظ بالمعرف
                                name = name, 
                                price = price.toDoubleOrNull() ?: 0.0, 
                                cost = cost.toDoubleOrNull() ?: 0.0,
                                barcode = if(barcode.isBlank()) null else barcode,
                                unit = if(unit.isBlank()) "حبة" else unit,
                                expiryDate = if(expiryDate.isBlank()) null else expiryDate,
                                quantity = quantity.toIntOrNull() ?: 0
                            )
                            
                            if (isEditing) {
                                dao.update(newProduct) // تحديث
                            } else {
                                dao.insert(newProduct) // إضافة جديدة
                            }
                            
                            showAddDialog = false
                            editingProduct = null
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
