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
import com.pocketcashier.data.Product
import com.pocketcashier.screens.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        setContent { MainAppScreen(db) }
    }
}

@Composable
fun MainAppScreen(db: AppDatabase) {
    var isLoggedIn by remember { mutableStateOf(false) }
    var isManager by remember { mutableStateOf(true) }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { manager -> 
            isManager = manager
            isLoggedIn = true 
        })
    } else {
        MainNavigation(db, isManager)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(db: AppDatabase, isManager: Boolean) {
    val navController = rememberNavController()
    val cashierCart = remember { mutableStateMapOf<Product, Int>() }
    var isDarkTheme by remember { mutableStateOf(false) }

    val items = listOf(
        BottomNavItem("الرئيسية", Icons.Default.Home, "dashboard"),
        BottomNavItem("البيع", Icons.Default.ShoppingCart, "cashier"),
        BottomNavItem("المنتجات", Icons.Default.Menu, "products"),
        BottomNavItem("العملاء", Icons.Default.Person, "customers"),
        BottomNavItem("المزيد", Icons.Default.MoreHoriz, "more")
    )

    MaterialTheme(colorScheme = if (isDarkTheme) darkColorScheme() else lightColorScheme()) {
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
                composable("cashier") { CashierScreen(db.productDao(), db.saleDao(), db.customerDao(), db.suspendedSaleDao(), cart = cashierCart, onScanClick = { navController.navigate("scanner") }) }
                composable("scanner") { BarcodeScannerScreen(db.productDao(), cart = cashierCart, onBack = { navController.popBackStack() }) }
                composable("products") { ProductsScreen(db.productDao()) }
                composable("customers") { CustomersScreen(db.customerDao()) }
                composable("more") { 
                    MoreScreen(
                        isManager = isManager,
                        onPurchasesClick = { navController.navigate("purchases") },
                        onExpensesClick = { navController.navigate("expenses") },
                        onReturnsClick = { navController.navigate("returns") },
                        onReportsClick = { navController.navigate("reports") },
                        onSettingsClick = { navController.navigate("settings") },
                        onCashSessionClick = { navController.navigate("cash_session") }
                    ) 
                }
                composable("cash_session") { CashSessionScreen(db.cashSessionDao(), onBack = { navController.popBackStack() }) }
                composable("settings") { SettingsScreen(db, isDarkTheme, { isDarkTheme = it }, { navController.popBackStack() }) }
                composable("purchases") { PurchasesScreen(db.purchaseDao()) }
                composable("expenses") { ExpensesScreen(db.expenseDao()) }
                composable("returns") { ReturnsScreen(db.returnDao()) }
                composable("reports") { ReportsScreen(db.saleDao(), db.expenseDao(), db.purchaseDao()) }
                composable("sales_history") { SalesHistoryScreen(db.saleDao(), onBack = { navController.popBackStack() }) }
            }
        }
    }
}

data class BottomNavItem(val title: String, val icon: ImageVector, val route: String)
