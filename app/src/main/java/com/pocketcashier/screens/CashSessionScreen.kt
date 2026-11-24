package com.pocketcashier.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.CashSession
import com.pocketcashier.data.CashSessionDao
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashSessionScreen(dao: CashSessionDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val activeSession by dao.getActiveSession().collectAsState(initial = null)
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    var showOpenDialog by remember { mutableStateOf(false) }
    var showCloseDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إدارة الصندوق") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            if (activeSession == null) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("الصندوق مغلق حالياً", style = MaterialTheme.typography.titleLarge)
                        Text("يجب فتح الصندوق لتسجيل المبيعات النقدية", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(onClick = { showOpenDialog = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("فتح الصندوق (بداية اليوم)")
                        }
                    }
                }
            } else {
                val session = activeSession!!
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, tint = Color(0xFF4CAF50))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الصندوق مفتوح", style = MaterialTheme.typography.titleLarge, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("وقت الفتح: ${dateFormat.format(Date(session.startTime))}")
                        Text("الرصيد الافتتاحي: ${session.openingBalance} ريال", fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCloseDialog = true }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("إغلاق الصندوق (نهاية اليوم)")
                        }
                    }
                }
            }
        }
    }

    if (showOpenDialog) {
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showOpenDialog = false },
            title = { Text("فتح الصندوق") },
            text = {
                Column {
                    Text("أدخل المبلغ النقدي الموجود في الدرج (الفكة):")
                    OutlinedTextField(
                        value = amount, 
                        onValueChange = { amount = it }, 
                        label = { Text("المبلغ") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val balance = amount.toDoubleOrNull() ?: 0.0
                    scope.launch {
                        dao.openSession(CashSession(openingBalance = balance))
                        showOpenDialog = false
                        Toast.makeText(context, "تم فتح الصندوق بنجاح", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("فتح") }
            },
            dismissButton = { TextButton(onClick = { showOpenDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showCloseDialog && activeSession != null) {
        var actualCashStr by remember { mutableStateOf("") }
        var expectedCash by remember { mutableStateOf(0.0) }
        
        LaunchedEffect(Unit) {
            val sales = dao.getCashSalesSince(activeSession!!.startTime) ?: 0.0
            val expenses = dao.getExpensesSince(activeSession!!.startTime) ?: 0.0
            expectedCash = activeSession!!.openingBalance + sales - expenses
        }

        AlertDialog(
            onDismissRequest = { showCloseDialog = false },
            title = { Text("إغلاق الصندوق") },
            text = {
                Column {
                    Text("النقدية المتوقعة (حسب النظام):", fontWeight = FontWeight.Bold)
                    Text("$expectedCash ريال", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("أدخل النقدية الفعلية الموجودة في الدرج:")
                    OutlinedTextField(
                        value = actualCashStr, 
                        onValueChange = { actualCashStr = it }, 
                        label = { Text("المبلغ الفعلي") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    
                    val actual = actualCashStr.toDoubleOrNull()
                    if (actual != null) {
                        val diff = actual - expectedCash
                        Spacer(modifier = Modifier.height(8.dp))
                        if (diff == 0.0) Text("مطابق تماماً ✔️", color = Color.Green, fontWeight = FontWeight.Bold)
                        else if (diff > 0) Text("زيادة: $diff ريال", color = Color.Blue, fontWeight = FontWeight.Bold)
                        else Text("عجز: ${-diff} ريال", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val actual = actualCashStr.toDoubleOrNull() ?: 0.0
                    val diff = actual - expectedCash
                    scope.launch {
                        dao.updateSession(activeSession!!.copy(
                            endTime = System.currentTimeMillis(),
                            expectedCash = expectedCash,
                            actualCash = actual,
                            difference = diff,
                            isOpen = false
                        ))
                        showCloseDialog = false
                        Toast.makeText(context, "تم إغلاق الصندوق بنجاح", Toast.LENGTH_SHORT).show()
                    }
                }) { Text("تأكيد الإغلاق") }
            },
            dismissButton = { TextButton(onClick = { showCloseDialog = false }) { Text("إلغاء") } }
        )
    }
}
