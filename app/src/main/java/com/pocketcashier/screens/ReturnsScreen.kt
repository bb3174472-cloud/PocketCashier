package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.ReturnDamage
import com.pocketcashier.data.ReturnDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(dao: ReturnDao) {
    val scope = rememberCoroutineScope()
    val returns by dao.getAllReturns().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("المرتجعات والتالف") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(returns) { item ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${item.type}: ${item.productName}", style = MaterialTheme.typography.titleMedium)
                        Text("الكمية: ${item.quantity}")
                    }
                }
            }
        }
    }

    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var qty by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("تالف") } // الافتراضي
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("تسجيل مرتجع/تالف") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم المنتج") })
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("الكمية") })
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = type == "تالف", onClick = { type = "تالف" })
                        Text("تالف")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = type == "مرتجع", onClick = { type = "مرتجع" })
                        Text("مرتجع")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        scope.launch {
                            dao.insertReturn(ReturnDamage(productName = name, quantity = qty.toIntOrNull() ?: 0, type = type))
                            showDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
