package com.pocketcashier.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Product
import com.pocketcashier.data.ProductDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(dao: ProductDao) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val products by dao.getAllProducts().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val csvHeader = "ID,الاسم,الباركود,سعر البيع,سعر الكرتون,سعر التكلفة,الكمية,الوحدة,حبات بالكرتون\n"
                        val csvData = products.joinToString(separator = "\n") { p ->
                            "${p.id},${p.name},${p.barcode ?: ""},${p.price},${p.wholesalePrice ?: ""},${p.cost},${p.quantity},${p.unit},${p.conversionFactor}"
                        }
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write((csvHeader + csvData).toByteArray())
                        }
                    }
                    Toast.makeText(context, "✅ تم تصدير المنتجات بنجاح", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "❌ فشل التصدير", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("إدارة المنتجات") },
                actions = {
                    IconButton(onClick = { exportLauncher.launch("Products_Export.csv") }) {
                        Icon(Icons.Default.Share, contentDescription = "تصدير CSV", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            ) 
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة منتج")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { editingProduct = product },
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${product.name} (${product.unit})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("بيع حبة: ${product.price} | شراء: ${product.cost}", style = MaterialTheme.typography.bodySmall)
                            if (product.conversionFactor > 1 && product.wholesalePrice != null) {
                                Text("بيع كرتون (${product.conversionFactor}): ${product.wholesalePrice}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            Text("الكمية المتوفرة: ${product.quantity}", color = if (product.quantity < 5) Color.Red else Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { scope.launch { dao.delete(product) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog || editingProduct != null) {
        val isEditing = editingProduct != null
        
        var name by remember { mutableStateOf(editingProduct?.name ?: "") }
        var price by remember { mutableStateOf(editingProduct?.price?.toString() ?: "") }
        var cost by remember { mutableStateOf(editingProduct?.cost?.toString() ?: "") }
        var wholesalePrice by remember { mutableStateOf(editingProduct?.wholesalePrice?.toString() ?: "") }
        var conversionFactor by remember { mutableStateOf(editingProduct?.conversionFactor?.toString() ?: "1") }
        var barcode by remember { mutableStateOf(editingProduct?.barcode ?: "") }
        var unit by remember { mutableStateOf(editingProduct?.unit ?: "حبة") }
        var expiryDate by remember { mutableStateOf(editingProduct?.expiryDate ?: "") }
        var quantity by remember { mutableStateOf(editingProduct?.quantity?.toString() ?: "") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false; editingProduct = null },
            title = { Text(if (isEditing) "تعديل منتج" else "منتج جديد") },
            text = {
                LazyColumn {
                    item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("سعر بيع الحبة") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = cost, onValueChange = { cost = it }, label = { Text("سعر التكلفة") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية الإجمالية (بالحبات)") }, modifier = Modifier.fillMaxWidth()) }
                    
                    item { 
                        Text("بيانات الكرتون (اختياري):", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                    }
                    item { OutlinedTextField(value = conversionFactor, onValueChange = { conversionFactor = it }, label = { Text("عدد الحبات في الكرتون (مثال: 24)") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = wholesalePrice, onValueChange = { wholesalePrice = it }, label = { Text("سعر بيع الكرتون") }, modifier = Modifier.fillMaxWidth()) }
                    
                    item { OutlinedTextField(value = barcode, onValueChange = { barcode = it }, label = { Text("الباركود") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("الوحدة الأساسية (حبة)") }, modifier = Modifier.fillMaxWidth()) }
                    item { OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("تاريخ الانتهاء") }, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            val newProduct = Product(
                                id = editingProduct?.id ?: 0,
                                name = name, 
                                price = price.toDoubleOrNull() ?: 0.0, 
                                cost = cost.toDoubleOrNull() ?: 0.0,
                                wholesalePrice = wholesalePrice.toDoubleOrNull(),
                                conversionFactor = conversionFactor.toIntOrNull() ?: 1,
                                barcode = if(barcode.isBlank()) null else barcode,
                                unit = if(unit.isBlank()) "حبة" else unit,
                                expiryDate = if(expiryDate.isBlank()) null else expiryDate,
                                quantity = quantity.toIntOrNull() ?: 0
                            )
                            if (isEditing) dao.update(newProduct) else dao.insert(newProduct)
                            showAddDialog = false
                            editingProduct = null
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
