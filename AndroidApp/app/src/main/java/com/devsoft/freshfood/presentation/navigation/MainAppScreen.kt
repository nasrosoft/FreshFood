package com.devsoft.freshfood.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.devsoft.freshfood.presentation.dashboard.DashboardScreen
import com.devsoft.freshfood.presentation.dashboard.DashboardViewModel
import com.devsoft.freshfood.presentation.products.ProductListScreen
import com.devsoft.freshfood.presentation.products.AddProductScreen
import com.devsoft.freshfood.presentation.products.ProductsViewModel
import com.devsoft.freshfood.presentation.sales.PosScreen
import com.devsoft.freshfood.presentation.sales.PosViewModel
import com.devsoft.freshfood.presentation.customers.CustomersScreen
import com.devsoft.freshfood.presentation.customers.AddCustomerScreen
import com.devsoft.freshfood.presentation.customers.CustomersViewModel

@Composable
fun MainAppScreen(
    dashboardViewModel: DashboardViewModel,
    productsViewModel: ProductsViewModel,
    posViewModel: PosViewModel,
    customersViewModel: CustomersViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom bar on nested screens like "add_product"
    val showBottomBar = currentRoute in listOf("dashboard", "pos", "products", "customers")
    
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                        label = { Text("Home") },
                        selected = currentRoute == "dashboard",
                        onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "POS") },
                        label = { Text("POS") },
                        selected = currentRoute == "pos",
                        onClick = { navController.navigate("pos") { launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.List, contentDescription = "Inventory") },
                        label = { Text("Inventory") },
                        selected = currentRoute == "products",
                        onClick = { navController.navigate("products") { launchSingleTop = true; restoreState = true } }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = "Customers") },
                        label = { Text("Customers") },
                        selected = currentRoute == "customers",
                        onClick = { navController.navigate("customers") { launchSingleTop = true; restoreState = true } }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = "dashboard", modifier = Modifier.padding(paddingValues)) {
            composable("dashboard") { DashboardScreen(dashboardViewModel) }
            composable("pos") { PosScreen(posViewModel) }
            composable("products") { 
                ProductListScreen(
                    viewModel = productsViewModel, 
                    onAddProductClick = { navController.navigate("add_product") }, 
                    onProductClick = {}
                ) 
            }
            composable("add_product") { AddProductScreen(onBack = { navController.popBackStack() }) }
            
            composable("customers") { 
                // We'll pass the add action via an assumption that CustomersScreen has it, 
                // but since CustomersScreen wasn't fully customized with a FAB yet, we'll route it later or wrap it.
                // For now, assume it's just viewing. We can add a FAB route in the future.
                CustomersScreen(customersViewModel) 
            }
            composable("add_customer") { AddCustomerScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
