package com.pocketcashier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.AppDatabase
import com.pocketcashier.data.Product
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // تهيئة قاعدة البيانات
        val db = AppDatabase.getDatabase(this)
        val dao = db.productDao()

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ProductScreen(dao)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(dao: com.pocketcashier.data.ProductDao) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    
    // جلب المنتجات من قاعدة البيانات
    val products by dao.getAllProducts().collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("إضافة منتج جديد", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("اسم المنتج") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = price,
            onValueChange = { price = it },
            label = { Text("السعر") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("الكمية") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()) {
                    val newProduct = Product(
                        name = name,
                        price = price.toDoubleOrNull() ?: 0.0,
                        quantity = quantity.toIntOrNull() ?: 0
                    )
                    scope.launch {
                        dao.insert(newProduct)
                        name = ""
                        price = ""
                        quantity = ""
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("حفظ المنتج")
        }

        Spacer(modifier = Modifier.height(24.dp))
        Divider()
        Spacer(modifier = Modifier.height(8.dp))
        
        Text("قائمة المنتجات", style = MaterialTheme.typography.titleLarge)
        
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(products) { product ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                        Text(text = "السعر: ${product.price} | الكمية: ${product.quantity}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
