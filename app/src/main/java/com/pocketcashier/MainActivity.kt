package com.pocketcashier

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
        setContent { MaterialTheme { MainAppScreen(db) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(db: AppDatabase) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem("الرئيسية", Icons.Default.Home, "dashboard"),
        BottomNavItem("البيع", Icons.Default.ShoppingCart, "cashier"),
        BottomNavItem("المنتجات", Icons.Default.Menu, "products"),
        BottomNavItem("العملاء", Icons.Default.Person, "customers"),
        BottomNavItem("المزيد", Icons.Default.MoreHoriz, "more")
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
            composable("dashboard") { DashboardScreen(db.productDao(), db.saleDao(), onViewSalesClick = { navController.navigate("sales_history") }) }
            
            composable("cashier") { entry ->
                val scannedBarcode = entry.savedStateHandle.getStateFlow("scanned_barcode", "").collectAsState()
                CashierScreen(
                    productDao = db.productDao(), 
                    saleDao = db.saleDao(),
                    scannedBarcode = scannedBarcode.value,
                    onBarcodeConsumed = { entry.savedStateHandle["scanned_barcode"] = "" },
                    onScanClick = { navController.navigate("scanner") }
                )
            }
            composable("scanner") {
                BarcodeScannerScreen(
                    onBarcodeScanned = { barcode ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("scanned_barcode", barcode)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("products") { ProductsScreen(db.productDao()) }
            composable("customers") { CustomersScreen(db.customerDao()) }
            composable("more") { MoreScreen(
                onPurchasesClick = { navController.navigate("purchases") },
                onExpensesClick = { navController.navigate("expenses") },
                onReturnsClick = { navController.navigate("returns") },
                onReportsClick = { navController.navigate("reports") }
            ) }
            composable("purchases") { PurchasesScreen(db.purchaseDao()) }
            composable("expenses") { ExpensesScreen(db.expenseDao()) }
            composable("returns") { ReturnsScreen(db.returnDao()) }
            composable("reports") { ReportsScreen(db.saleDao(), db.expenseDao(), db.purchaseDao()) }
            composable("sales_history") { SalesHistoryScreen(db.saleDao(), onBack = { navController.popBackStack() }) }
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
