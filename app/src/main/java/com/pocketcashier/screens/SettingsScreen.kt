package com.pocketcashier.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
fun SettingsScreen(
    db: AppDatabase,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    var showResetDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    // النسخ الاحتياطي
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val dbFile = context.getDatabasePath("pocket_cashier_db")
                        context.contentResolver.openOutputStream(uri)?.use { out ->
                            dbFile.inputStream().use { it.copyTo(out) }
                        }
                    }
                    Toast.makeText(context, "✅ تم حفظ النسخة بنجاح", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(context, "❌ فشل الحفظ", Toast.LENGTH_LONG).show() }
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
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            dbFile.outputStream().use { input.copyTo(it) }
                        }
                    }
                    Toast.makeText(context, "✅ تمت الاستعادة! أعد تشغيل التطبيق.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) { Toast.makeText(context, "❌ فشل الاستعادة", Toast.LENGTH_LONG).show() }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("الإعدادات", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "رجوع") } }) }
    ) { padding ->
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
                Row(modifier = Modifier.padding(16.dp).fillMaxWidth().clickable { showPinDialog = true }, horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Lock, null); Spacer(Modifier.width(8.dp)); Text("تغيير الرقم السري") }
                    Icon(Icons.Default.KeyboardArrowLeft, null)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("النسخ الاحتياطي", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { exportLauncher.launch("backup_${System.currentTimeMillis()}.db") }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Backup, null); Spacer(Modifier.width(4.dp)); Text("حفظ") }
                        Button(onClick = { importLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(4.dp)); Text("استعادة") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("البيانات", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Button(onClick = { showResetDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Icon(Icons.Default.DeleteForever, null); Spacer(Modifier.width(8.dp)); Text("مسح كل البيانات")
                }
            }
        }
    }

    // نافذة تغيير الرقم السري
    if (showPinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("تغيير الرقم السري") },
            text = {
                Column {
                    OutlinedTextField(value = currentPin, onValueChange = { if(it.length <= 4) currentPin = it }, label = { Text("الرقم الحالي") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = newPin, onValueChange = { if(it.length <= 4) newPin = it }, label = { Text("الرقم الجديد") }, visualTransformation = PasswordVisualTransformation())
                    OutlinedTextField(value = confirmPin, onValueChange = { if(it.length <= 4) confirmPin = it }, label = { Text("تأكيد الرقم الجديد") }, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val savedPin = prefs.getString("app_pin", "1234")
                    if (currentPin != savedPin) {
                        Toast.makeText(context, "الرقم الحالي غير صحيح", Toast.LENGTH_SHORT).show()
                    } else if (newPin.length != 4) {
                        Toast.makeText(context, "الرقم الجديد يجب أن يكون 4 أرقام", Toast.LENGTH_SHORT).show()
                    } else if (newPin != confirmPin) {
                        Toast.makeText(context, "الرقم الجديد غير متطابق", Toast.LENGTH_SHORT).show()
                    } else {
                        prefs.edit().putString("app_pin", newPin).apply()
                        Toast.makeText(context, "✅ تم تغيير الرقم السري بنجاح", Toast.LENGTH_SHORT).show()
                        showPinDialog = false
                    }
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text("إلغاء") } }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("تحذير!") },
            text = { Text("هل أنت متأكد من مسح كل البيانات؟") },
            confirmButton = {
                TextButton(onClick = { scope.launch { withContext(Dispatchers.IO) { db.clearAllTables() }; showResetDialog = false; Toast.makeText(context, "تم المسح", Toast.LENGTH_SHORT).show() } }, colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)) { Text("نعم") }
            }
        )
    }
}
