package com.pocketcashier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.AppDatabase
import com.pocketcashier.data.Product
import com.pocketcashier.data.ProductDao
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val db = AppDatabase.getDatabase(this)
        val dao = db.productDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CashierScreen(dao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(dao: ProductDao) {
    val scope = rememberCoroutineScope()
    val products by dao.getAllProducts().collectAsState(initial = emptyList())
    
    val cart = remember { mutableStateMapOf<Product, Int>() }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val totalAmount = cart.entries.sumOf { it.key.price * it.value }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("كاشير الجيب - البيع", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showAddProductDialog = true }) {
                        Text("إضافة منتج")
                    }
                }
            )
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الإجمالي", style = MaterialTheme.typography.titleMedium)
                            Text("${totalAmount} ريال", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    cart.forEach { (product, qty) ->
                                        dao.updateQuantity(product.id, qty)
                                    }
                                    cart.clear()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(0xFF4CAF50))
                        ) {
                            Text("إتمام البيع")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp)) {
            Text("اضغط على المنتج لإضافته للسلة", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(products) { product ->
                    val qtyInCart = cart[product] ?: 0
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable(enabled = product.quantity > qtyInCart) {
                                cart[product] = qtyInCart + 1
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (product.quantity > qtyInCart) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = "المتوفر: ${product.quantity} | السعر: ${product.price}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (qtyInCart > 0) {
                                Text(text = "في السلة: $qtyInCart", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProductDialog) {
        AddProductDialog(dao = dao, onDismiss = { showAddProductDialog = false })
    }
}

@Composable
fun AddProductDialog(dao: ProductDao, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة منتج جديد") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المنتج") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("السعر") })
                OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("الكمية") })
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()) {
                        scope.launch {
                            dao.insert(Product(name = name, price = price.toDoubleOrNull() ?: 0.0, quantity = quantity.toIntOrNull() ?: 0))
                            onDismiss()
                        }
                    }
                }
            ) { Text("حفظ") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}
