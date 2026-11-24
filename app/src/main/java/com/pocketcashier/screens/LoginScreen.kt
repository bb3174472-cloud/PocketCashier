package com.pocketcashier.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pocketcashier.AppGold
import com.pocketcashier.AppNavy
import com.pocketcashier.AppBlue

@Composable
fun LoginScreen(onLoginSuccess: (isManager: Boolean) -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    
    val managerPin = prefs.getString("app_pin", "1234") ?: "1234"
    val cashierPin = prefs.getString("cashier_pin", "0000") ?: "0000"
    
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(AppNavy, AppBlue))),
        contentAlignment = Alignment.Center
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Storefront, contentDescription = null, tint = AppGold, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("كاشير الخلاقي", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("أدخل الرقم السري للمتابعة", fontSize = 16.sp, color = AppGold)
            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                repeat(4) { index ->
                    val isFilled = index < enteredPin.length
                    Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(if (isFilled) AppGold else Color.White.copy(alpha = 0.3f)))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            if (errorMessage.isNotEmpty()) Text(errorMessage, color = Color.Red, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(32.dp))

            val buttons = listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("C", "0", "OK"))
            buttons.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { label ->
                        if (label == "OK") {
                            IconButton(onClick = {
                                if (enteredPin == managerPin) onLoginSuccess(true)
                                else if (enteredPin == cashierPin) onLoginSuccess(false)
                                else { errorMessage = "رقم خاطئ"; enteredPin = "" }
                            }, modifier = Modifier.size(72.dp)) { Text("دخول", color = AppGold, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                        } else if (label == "C") {
                            IconButton(onClick = { if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1); errorMessage = "" }, modifier = Modifier.size(72.dp)) { Icon(Icons.Default.Backspace, null, tint = Color.White) }
                        } else {
                            Button(
                                onClick = { if (enteredPin.length < 4) { enteredPin += label; errorMessage = "" } }, 
                                modifier = Modifier.size(72.dp), 
                                shape = CircleShape, 
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(label, fontSize = 24.sp, color = Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
