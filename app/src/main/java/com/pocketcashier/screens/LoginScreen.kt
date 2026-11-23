package com.pocketcashier.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    // الرقم السري الافتراضي هو 1234 (إذا لم يقم المستخدم بتغييره لاحقاً)
    val savedPin = prefs.getString("app_pin", "1234") ?: "1234"
    
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("مرحباً بك في كاشير الجيب", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("أدخل الرقم السري للمتابعة", fontSize = 16.sp)
        Spacer(modifier = Modifier.height(24.dp))

        // عرض النقاط التي يتم إدخالها
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            repeat(4) { index ->
                val isFilled = index < enteredPin.length
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(24.dp)
                ) {}
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        if (errorMessage.isNotEmpty()) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // لوحة الأرقام
        val buttons = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "OK")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { label ->
                    if (label == "OK") {
                        IconButton(onClick = {
                            if (enteredPin == savedPin) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "الرقم السري غير صحيح"
                                enteredPin = ""
                            }
                        }) { Text("دخول", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                    } else if (label == "C") {
                        IconButton(onClick = { 
                            if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
                            errorMessage = ""
                        }) { Icon(Icons.Default.Backspace, contentDescription = "مسح") }
                    } else {
                        Button(
                            onClick = { 
                                if (enteredPin.length < 4) {
                                    enteredPin += label
                                    errorMessage = ""
                                }
                            },
                            modifier = Modifier.size(72.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(label, fontSize = 24.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
