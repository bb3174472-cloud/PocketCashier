package com.pocketcashier.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Sale
import com.pocketcashier.data.SaleDao
import com.pocketcashier.data.SaleItem
import com.pocketcashier.utils.generateInvoicePdf
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(dao: SaleDao, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sales by dao.getAllSales().collectAsState(initial = emptyList())
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    var selectedSale by remember { mutableStateOf<Sale?>(null) }
    var saleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }

    // أداة حفظ PDF
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null && selectedSale != null) {
            val success = generateInvoicePdf(context, uri, selectedSale!!, saleItems)
            if (success) Toast.makeText(context, "✅ تم حفظ الفاتورة كـ PDF", Toast.LENGTH_LONG).show()
            else Toast.makeText(context, "❌ فشل حفظ الملف", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("سجل المبيعات") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            items(sales) { sale ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { 
                        selectedSale = sale
                        scope.launch { saleItems = dao.getSaleItems(sale.id) }
                    }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("فاتورة رقم: ${sale.id}", fontWeight = FontWeight.Bold)
                            Text(dateFormat.format(Date(sale.timestamp)), style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي: ${sale.totalAmount} ريال", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(sale.paymentType, color = if (sale.paymentType == "نقدي") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    // نافذة تفاصيل الفاتورة
    if (selectedSale != null) {
        val sale = selectedSale!!
        AlertDialog(
            onDismissRequest = { selectedSale = null },
            title = { 
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("فاتورة ${sale.id}")
                    Row {
                        // زر حفظ PDF
                        IconButton(onClick = { pdfLauncher.launch("Invoice_${sale.id}.pdf") }) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = MaterialTheme.colorScheme.error)
                        }
                        // زر مشاركة نصية
                        IconButton(onClick = {
                            val shareText = buildString {
                                appendLine("🧾 *فاتورة رقم ${sale.id}*")
                                saleItems.forEach { item -> appendLine("${item.productName} × ${item.quantity} = ${item.price * item.quantity}") }
                                appendLine("الإجمالي: *${sale.totalAmount} ريال*")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "مشاركة"))
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            text = {
                LazyColumn {
                    items(saleItems) { item ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(item.productName)
                            Text("${item.quantity} × ${item.price}")
                        }
                        Divider()
                    }
                    if (sale.discount > 0) {
                        item {
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("الخصم", color = MaterialTheme.colorScheme.error)
                                Text("-${sale.discount}", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedSale = null }) { Text("إغلاق") } }
        )
    }
}
