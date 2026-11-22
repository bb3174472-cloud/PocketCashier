package com.pocketcashier.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    productDao: ProductDao, 
    saleDao: SaleDao,
    scannedBarcode: String = "",
    onBarcodeConsumed: () -> Unit = {},
    onScanClick: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    val cart = remember { mutableStateMapOf<Product, Int>() }
    val totalAmount = cart.entries.sumOf { it.key.price * it.value }
    
    var showManualEntry by remember { mutableStateOf(false) }

    // دالة مساعدة للبحث عن المنتج وإضافته
    fun processBarcode(barcode: String) {
        val cleanScanned = barcode.trim()
        // البحث المطابق تماماً
        var product = products.find { it.barcode?.trim() == cleanScanned }
        
        // حل مشكلة الباركود 12 رقم (إذا أضافت الكاميرا صفراً في البداية)
        if (product == null && cleanScanned.length == 13 && cleanScanned.startsWith("0")) {
            val codeWithoutZero = cleanScanned.substring(1)
            product = products.find { it.barcode?.trim() == codeWithoutZero }
        }
        // حل عكسي (إذا كان المحفوظ 13 والمقروء 12)
        if (product == null && cleanScanned.length == 12) {
            val codeWithZero = "0$cleanScanned"
            product = products.find { it.barcode?.trim() == codeWithZero }
        }

        if (product != null) {
            val qtyInCart = cart[product] ?: 0
            if (product.quantity > qtyInCart) {
                cart[product] = qtyInCart + 1
                Toast.makeText(context, "تمت إضافة ${product.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "الكمية غير كافية في المخزون!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "غير مسجل: $cleanScanned", Toast.LENGTH_LONG).show()
        }
    }

    // التقاط الباركود من الكاميرا
    LaunchedEffect(scannedBarcode, products) {
        if (scannedBarcode.isNotBlank() && products.isNotEmpty()) {
            processBarcode(scannedBarcode)
            onBarcodeConsumed()
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("شاشة البيع", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showManualEntry = true }) {
                        Icon(Icons.Default.Keyboard, contentDescription = "إدخال يدوي")
                    }
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "مسح باركود", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            ) 
        },
        bottomBar = {
            if (cart.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("الإجمالي", style = MaterialTheme.typography.titleMedium)
                            Text("$totalAmount ريال", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    val saleId = saleDao.insertSale(Sale(totalAmount = totalAmount))
                                    val saleItems = cart.map { (product, qty) -> SaleItem(saleId = saleId.toInt(), productName = product.name, price = product.price, quantity = qty) }
                                    saleDao.insertSaleItems(saleItems)
                                    cart.forEach { (product, qty) -> productDao.updateQuantity(product.id, qty) }
                                    cart.clear()
                                    Toast.makeText(context, "تم إتمام البيع بنجاح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("إتمام البيع") }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(products) { product ->
                val qtyInCart = cart[product] ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable(enabled = product.quantity > qtyInCart) { cart[product] = qtyInCart + 1 },
                    colors = CardDefaults.cardColors(containerColor = if (product.quantity > qtyInCart) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("متوفر: ${product.quantity} | سعر: ${product.price}")
                            if (!product.barcode.isNullOrBlank()) {
                                Text("باركود: ${product.barcode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        if (qtyInCart > 0) {
                            Text("في السلة: $qtyInCart", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showManualEntry) {
        var manualCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManualEntry = false },
            title = { Text("إدخال باركود يدوياً") },
            text = {
                OutlinedTextField(
                    value = manualCode,
                    onValueChange = { manualCode = it },
                    label = { Text("رقم الباركود") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (manualCode.isNotBlank()) {
                        processBarcode(manualCode)
                        manualCode = ""
                        showManualEntry = false
                    }
                }) { Text("إضافة") }
            }
        )
    }
}
