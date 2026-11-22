package com.pocketcashier.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pocketcashier.data.Expense
import com.pocketcashier.data.ExpenseDao
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(dao: ExpenseDao) {
    val scope = rememberCoroutineScope()
    val expenses by dao.getAllExpenses().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("سجل المصروفات") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showDialog = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(expense.description, style = MaterialTheme.typography.titleMedium)
                        Text("المبلغ: ${expense.amount} ريال", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showDialog) {
        var desc by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("إضافة مصروف") },
            text = {
                Column {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("الوصف (مثال: إيجار)") })
                    OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("المبلغ") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (desc.isNotBlank()) {
                        scope.launch {
                            dao.insertExpense(Expense(description = desc, amount = amount.toDoubleOrNull() ?: 0.0))
                            showDialog = false
                        }
                    }
                }) { Text("حفظ") }
            }
        )
    }
}
