package com.pocketcashier.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.*
import kotlinx.coroutines.launch
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    productDao: ProductDao, 
    saleDao: SaleDao,
    customerDao: CustomerDao,
    suspendedDao: SuspendedSaleDao,
    cart: MutableMap<Product, Int>,
    onScanClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val products by productDao.getAllProducts().collectAsState(initial = emptyList())
    val customers by customerDao.getAllCustomers().collectAsState(initial = emptyList())
    val suspendedSales by suspendedDao.getAllSuspended().collectAsState(initial = emptyList())
    
    val totalAmount = cart.entries.sumOf { it.key.price * it.value }
    
    var showManualEntry by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showSuspendedDialog by remember { mutableStateOf(false) }

    fun processManualBarcode(barcode: String) {
        val cleanScanned = barcode.trim()
        var product = products.find { it.barcode?.trim() == cleanScanned }
        if (product != null) {
            val qtyInCart = cart[product] ?: 0
            if (product.quantity > qtyInCart) {
                cart[product] = qtyInCart + 1
                Toast.makeText(context, "تمت إضافة ${product.name}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "غير مسجل: $cleanScanned", Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { Text("شاشة البيع", fontWeight = FontWeight.Bold) },
                actions = {
                    if (suspendedSales.isNotEmpty()) {
                        IconButton(onClick = { showSuspendedDialog = true }) {
                            Icon(Icons.Default.PauseCircle, contentDescription = "الفواتير المعلقة", tint = Color(0xFFFF9800))
                        }
                    }
                    IconButton(onClick = { showManualEntry = true }) {
                        Icon(Icons.Default.Keyboard, contentDescription = "إدخال يدوي")
                    }
                    IconButton(onClick = onScanClick) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "فتح الكاميرا", tint = MaterialTheme.colorScheme.primary)
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
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { 
                                    // حفظ الفاتورة كمعلقة
                                    val jsonObject = JSONObject()
                                    cart.forEach { (p, q) -> jsonObject.put(p.id.toString(), q) }
                                    scope.launch {
                                        suspendedDao.insertSuspendedSale(SuspendedSale(cartData = jsonObject.toString()))
                                        cart.clear()
                                        Toast.makeText(context, "تم تعليق الفاتورة", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                            ) { Text("تعليق") }
                            
                            Button(
                                onClick = { showCheckoutDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) { Text("الدفع") }
                        }
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
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("متوفر: ${product.quantity} | سعر: ${product.price}")
                        }
                        if (qtyInCart > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (qtyInCart == 1) cart.remove(product) else cart[product] = qtyInCart - 1 }) {
                                    Icon(Icons.Default.Remove, contentDescription = "تقليل", tint = Color.Red)
                                }
                                Text("$qtyInCart", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                                IconButton(onClick = { if (product.quantity > qtyInCart) cart[product] = qtyInCart + 1 }) {
                                    Icon(Icons.Default.Add, contentDescription = "زيادة", tint = Color(0xFF4CAF50))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // نافذة الدفع (الخصم، الجزئي، الآجل)
    if (showCheckoutDialog) {
        var discountStr by remember { mutableStateOf("0") }
        var paidStr by remember { mutableStateOf(totalAmount.toString()) }
        var selectedCustomerId by remember { mutableStateOf<Int?>(null) }
        
        val discount = discountStr.toDoubleOrNull() ?: 0.0
        val finalTotal = totalAmount - discount
        val paid = paidStr.toDoubleOrNull() ?: 0.0
        val remaining = finalTotal - paid

        AlertDialog(
            onDismissRequest = { showCheckoutDialog = false },
            title = { Text("إتمام عملية الدفع") },
            text = {
                Column {
                    Text("الإجمالي: $totalAmount ريال", fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = discountStr, 
                        onValueChange = { discountStr = it }, 
                        label = { Text("الخصم (إن وجد)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Text("المطلوب: $finalTotal ريال", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = paidStr, 
                        onValueChange = { paidStr = it }, 
                        label = { Text("المبلغ المدفوع (نقداً)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    if (remaining > 0) {
                        Text("المتبقي (دين): $remaining ريال", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text("اختر العميل لتسجيل الدين:", style = MaterialTheme.typography.bodySmall)
                        // قائمة منسدلة مبسطة لاختيار العميل
                        LazyColumn(modifier = Modifier.heightIn(max = 120.dp)) {
                            items(customers) { customer ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { selectedCustomerId = customer.id }.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selectedCustomerId == customer.id, onClick = { selectedCustomerId = customer.id })
                                    Text(customer.name)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (remaining > 0 && selectedCustomerId == null) {
                            Toast.makeText(context, "يجب اختيار عميل لتسجيل الدين", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        scope.launch {
                            val customerName = customers.find { it.id == selectedCustomerId }?.name
                            val paymentType = if (remaining <= 0) "نقدي" else if (paid == 0.0) "آجل" else "جزئي"
                            
                            // 1. حفظ الفاتورة
                            val saleId = saleDao.insertSale(Sale(
                                totalAmount = finalTotal, 
                                discount = discount,
                                paidAmount = paid,
                                paymentType = paymentType,
                                customerId = selectedCustomerId,
                                customerName = customerName
                            ))
                            
                            // 2. حفظ المنتجات وخصم المخزون
                            val saleItems = cart.map { (product, qty) -> SaleItem(saleId = saleId.toInt(), productName = product.name, price = product.price, quantity = qty) }
                            saleDao.insertSaleItems(saleItems)
                            cart.forEach { (product, qty) -> productDao.updateQuantity(product.id, qty) }
                            
                            // 3. تسجيل الدين للعميل
                            if (remaining > 0 && selectedCustomerId != null) {
                                customerDao.updateBalance(selectedCustomerId!!, remaining)
                            }
                            
                            cart.clear()
                            showCheckoutDialog = false
                            Toast.makeText(context, "تمت العملية بنجاح", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("تأكيد الدفع") }
            },
            dismissButton = { TextButton(onClick = { showCheckoutDialog = false }) { Text("إلغاء") } }
        )
    }

    // نافذة استعادة الفواتير المعلقة
    if (showSuspendedDialog) {
        AlertDialog(
            onDismissRequest = { showSuspendedDialog = false },
            title = { Text("الفواتير المعلقة") },
            text = {
                LazyColumn {
                    items(suspendedSales) { suspended ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("فاتورة معلقة", fontWeight = FontWeight.Bold)
                                Button(onClick = {
                                    // استعادة السلة
                                    try {
                                        val json = JSONObject(suspended.cartData)
                                        cart.clear()
                                        json.keys().forEach { key ->
                                            val product = products.find { it.id == key.toInt() }
                                            if (product != null) cart[product] = json.getInt(key)
                                        }
                                        scope.launch { suspendedDao.deleteSuspendedSale(suspended.id) }
                                        showSuspendedDialog = false
                                    } catch (e: Exception) { Toast.makeText(context, "خطأ في الاستعادة", Toast.LENGTH_SHORT).show() }
                                }) { Text("استعادة") }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showSuspendedDialog = false }) { Text("إغلاق") } }
        )
    }

    if (showManualEntry) {
        var manualCode by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showManualEntry = false },
            title = { Text("إدخال باركود يدوياً") },
            text = { OutlinedTextField(value = manualCode, onValueChange = { manualCode = it }, label = { Text("رقم الباركود") }) },
            confirmButton = {
                TextButton(onClick = { if (manualCode.isNotBlank()) { processManualBarcode(manualCode); manualCode = ""; showManualEntry = false } }) { Text("إضافة") }
            }
        )
    }
}
