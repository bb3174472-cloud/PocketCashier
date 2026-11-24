package com.pocketcashier.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(db: AppDatabase, isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var showCashierPinDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val dbFile = context.getDatabasePath("pocket_cashier_db")
                        context.contentResolver.openOutputStream(uri)?.use { out -> dbFile.inputStream().use { it.copyTo(out) } }
                    }
                    Toast.makeText(context, "✅ تم حفظ النسخة", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Toast.makeText(context, "❌ فشل", Toast.LENGTH_SHORT).show() }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val dbFile = context.getDatabasePath("pocket_cashier_db")
                        db.close()
                        context.contentResolver.openInputStream(uri)?.use { input -> dbFile.outputStream().use { input.copyTo(it) } }
                    }
                    Toast.makeText(context, "✅ تمت الاستعادة! أعد تشغيل التطبيق.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(context, "❌ فشل", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("الإعدادات") }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            
            Text("المظهر", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DarkMode, null); Spacer(Modifier.width(8.dp)); Text("الوضع الداكن") }
                    Switch(checked = isDarkTheme, onCheckedChange = onThemeChange)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("الأمان", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth().clickable { showPinDialog = true }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, null); Spacer(Modifier.width(8.dp)); Text("تغيير رقم المدير") }
                        Icon(Icons.Default.KeyboardArrowLeft, null)
                    }
                    Divider()
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth().clickable { showCashierPinDialog = true }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Person, null); Spacer(Modifier.width(8.dp)); Text("تغيير رقم البائع") }
                        Icon(Icons.Default.KeyboardArrowLeft, null)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("النسخ الاحتياطي", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("backup_${System.currentTimeMillis()}.db") }, modifier = Modifier.weight(1f)) { Text("حفظ") }
                        Button(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Text("استعادة") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("البيانات", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Button(onClick = { showResetDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("مسح كل البيانات") }
            }
        }
    }

    // نافذة تغيير رقم المدير
    if (showPinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تغيير رقم المدير") },
            text = {
                Column {
                    OutlinedTextField(value = currentPin, onValueChange = { if(it.length <= 4) currentPin = it }, label = { Text("الرقم الحالي") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newPin, onValueChange = { if(it.length <= 4) newPin = it }, label = { Text("الرقم الجديد (4 أرقام)") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentPin == prefs.getString("app_pin", "1234") && newPin.length == 4) {
                        prefs.edit().putString("app_pin", newPin).apply()
                        showPinDialog = false
                        Toast.makeText(context, "تم التغيير", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "خطأ في البيانات", Toast.LENGTH_SHORT).show()
                }) { Text("حفظ") }
            }
        )
    }

    // نافذة تغيير رقم البائع
    if (showCashierPinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCashierPinDialog = false },
            title = { Text("تغيير رقم البائع") },
            text = {
                Column {
                    OutlinedTextField(value = currentPin, onValueChange = { if(it.length <= 4) currentPin = it }, label = { Text("رقم المدير الحالي") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newPin, onValueChange = { if(it.length <= 4) newPin = it }, label = { Text("رقم البائع الجديد (4 أرقام)") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (currentPin == prefs.getString("app_pin", "1234") && newPin.length == 4) {
                        prefs.edit().putString("cashier_pin", newPin).apply()
                        showCashierPinDialog = false
                        Toast.makeText(context, "تم التغيير", Toast.LENGTH_SHORT).show()
                    } else Toast.makeText(context, "خطأ: يجب إدخال رقم المدير", Toast.LENGTH_SHORT).show()
                }) { Text("حفظ") }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تحذير!") },
            text = { Text("هل أنت متأكد من مسح كل البيانات؟") },
            confirmButton = { TextButton(onClick = { scope.launch { withContext(Dispatchers.IO) { db.clearAllTables() }; showResetDialog = false; Toast.makeText(context, "تم المسح", Toast.LENGTH_SHORT).show() } }) { Text("نعم") } }
        )
    }
}
