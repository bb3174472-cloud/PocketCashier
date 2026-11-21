package com.pocketcashier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pocketcashier.data.AppDatabase
import com.pocketcashier.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        
        setContent {
            MaterialTheme {
                MainAppScreen(db.productDao(), db.customerDao())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(productDao: com.pocketcashier.data.ProductDao, customerDao: com.pocketcashier.data.CustomerDao) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("الرئيسية", Icons.Default.Home, "dashboard"),
        BottomNavItem("البيع", Icons.Default.ShoppingCart, "cashier"),
        BottomNavItem("المنتجات", Icons.Default.Menu, "products"),
        BottomNavItem("العملاء", Icons.Default.Person, "customers"), // التبويب الجديد
        BottomNavItem("الإعدادات", Icons.Default.Settings, "settings")
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(padding)) {
            composable("dashboard") { DashboardScreen(productDao) }
            composable("cashier") { CashierScreen(productDao) }
            composable("products") { ProductsScreen(productDao) }
            composable("customers") { CustomersScreen(customerDao) }
            composable("settings") { SettingsScreen() }
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
