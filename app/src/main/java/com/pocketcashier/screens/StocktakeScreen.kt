package com.pocketcashier.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.ProductDao
import com.pocketcashier.data.Stocktake
import com.pocketcashier.data.StocktakeDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StocktakeScreen(productDao: ProductDao, stocktakeDao: StocktakeDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    val stocktakes by stocktakeDao.getAllStocktakes().collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الجرد الفعلي") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "إضافة جرد")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            if (stocktakes.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Text("لا توجد عمليات جرد سابقة.\nاضغط + لبدء جرد جديد.", textAlign = TextAlign.Center, color = Color.Gray)
                    }
                }
            }
            items(stocktakes) { take ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("المنتج: ${take.productName}", fontWeight = FontWeight.Bold)
                        Text("كمية النظام: ${take.systemQuantity} | الكمية الفعلية: ${take.actualQuantity}")
                        Text(
                            text = if (take.difference == 0) "مطابق" else if (take.difference > 0) "زيادة: ${take.difference}" else "عجز: ${-take.difference}",
                            color = if (take.difference == 0) Color(0xFF4CAF50) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var selectedProductName by remember { mutableStateOf("") }
        var actualQtyStr by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسجيل جرد جديد") },
            text = {
                Column {
                    Text("اختر المنتج:")
                    LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                        items(products) { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { selectedProductName = p.name },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedProductName == p.name, onClick = { selectedProductName = p.name })
                                Text(p.name)
                            }
                        }
                    }
                    OutlinedTextField(
                        value = actualQtyStr, 
                        onValueChange = { actualQtyStr = it }, 
                        label = { Text("الكمية الفعلية (الموجودة)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val product = products.find { it.name == selectedProductName }
                    val actualQty = actualQtyStr.toIntOrNull() ?: 0
                    if (product != null) {
                        scope.launch {
                            val diff = actualQty - product.quantity
                            stocktakeDao.insertStocktake(Stocktake(
                                productName = product.name,
                                systemQuantity = product.quantity,
                                actualQuantity = actualQty,
                                difference = diff
                            ))
                            productDao.updateQuantityDifference(product.id, diff)
                            showAddDialog = false
                            Toast.makeText(context, "تم الجرد بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
