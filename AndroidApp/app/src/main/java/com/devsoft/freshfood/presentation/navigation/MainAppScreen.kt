package com.devsoft.freshfood.presentation.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
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
import com.devsoft.freshfood.presentation.purchases.PurchaseViewModel
import com.devsoft.freshfood.presentation.deliveries.DeliveryViewModel
import com.devsoft.freshfood.presentation.deliveries.DeliveryDashboardScreen
import com.devsoft.freshfood.presentation.deliveries.DeliveryDetailsScreen
import com.devsoft.freshfood.presentation.users.UserManagementScreen
import com.devsoft.freshfood.presentation.inventory.InventoryViewModel
import com.devsoft.freshfood.presentation.inventory.ReturnsViewModel
import com.devsoft.freshfood.presentation.inventory.PhysicalInventoryScreen
import com.devsoft.freshfood.presentation.inventory.ReturnsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument

@Composable
fun MainAppScreen(
    userId: String?,
    userRole: String,
    dashboardViewModel: DashboardViewModel,
    productsViewModel: ProductsViewModel,
    posViewModel: PosViewModel,
    customersViewModel: CustomersViewModel,
    purchaseViewModel: PurchaseViewModel,
    deliveryViewModel: DeliveryViewModel,
    inventoryViewModel: InventoryViewModel,
    returnsViewModel: ReturnsViewModel,
    profileRepository: com.devsoft.freshfood.domain.repository.ProfileRepository,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"
    val context = androidx.compose.ui.platform.LocalContext.current

    // Sync scheduling removed

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    "FreshFood Admin",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(16.dp))

                val isDelivery = userRole == "DELIVERY"
                if (!isDelivery) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Dashboard") },
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                        label = { Text("Point of Sale") },
                        selected = currentRoute == "pos",
                        onClick = {
                            navController.navigate("pos") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null) },
                    label = { Text("Deliveries") },
                    selected = currentRoute == "deliveries",
                    onClick = {
                        navController.navigate("deliveries") { launchSingleTop = true; restoreState = true }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )

                if (!isDelivery) {
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text("Products") },
                        selected = currentRoute == "products",
                        onClick = {
                            navController.navigate("products") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Customers") },
                        selected = currentRoute == "customers",
                        onClick = {
                            navController.navigate("customers") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        label = { Text("Purchase Entry") },
                        selected = currentRoute == "purchases",
                        onClick = {
                            navController.navigate("purchases") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text("Physical Inventory") },
                        selected = currentRoute == "inventory",
                        onClick = {
                            navController.navigate("inventory") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                        label = { Text("Customer Returns") },
                        selected = currentRoute == "returns",
                        onClick = {
                            navController.navigate("returns") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                    NavigationDrawerItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Users / Drivers") },
                        selected = currentRoute == "users",
                        onClick = {
                            navController.navigate("users") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Filled.ExitToApp, contentDescription = "Log out", tint = MaterialTheme.colorScheme.error) },
                    label = { Text("Log out", color = MaterialTheme.colorScheme.error) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp)
                )
            }
        }
    ) {
        val startDest = if (userRole == "DELIVERY") "deliveries" else "dashboard"
        NavHost(navController = navController, startDestination = startDest, modifier = Modifier.fillMaxSize()) {
            composable("dashboard") { 
                DashboardScreen(dashboardViewModel, onOpenDrawer = { scope.launch { drawerState.open() } }) 
            }
            composable("pos") { 
                PosScreen(
                    viewModel = posViewModel,
                    productsViewModel = productsViewModel,
                    customersViewModel = customersViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                ) 
            }
            composable("products") { 
                ProductListScreen(
                    viewModel = productsViewModel, 
                    onAddProductClick = { barcode -> 
                        if (barcode != null) {
                            navController.navigate("add_product?barcode=$barcode") 
                        } else {
                            navController.navigate("add_product")
                        }
                    }, 
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                ) 
            }
            
            composable(
                route = "add_product?barcode={barcode}",
                arguments = listOf(navArgument("barcode") { 
                    type = NavType.StringType
                    nullable = true
                })
            ) { backStackEntry ->
                val barcode = backStackEntry.arguments?.getString("barcode")
                AddProductScreen(
                    viewModel = productsViewModel,
                    initialBarcode = barcode,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("purchases") {
                com.devsoft.freshfood.presentation.purchases.PurchaseEntryScreen(
                    viewModel = purchaseViewModel,
                    productsViewModel = productsViewModel,
                    onBack = { scope.launch { drawerState.open() } }
                )
            }

            composable("customers") { 
                CustomersScreen(
                    viewModel = customersViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onAddCustomerClick = { navController.navigate("add_customer") }
                ) 
            }
            composable("add_customer") { 
                AddCustomerScreen(
                    viewModel = customersViewModel,
                    onBack = { navController.popBackStack() }
                ) 
            }
            
            composable("deliveries") {
                DeliveryDashboardScreen(
                    viewModel = deliveryViewModel,
                    currentUserId = userId,
                    currentUserRole = userRole,
                    onDeliveryClick = { id -> navController.navigate("delivery_details/$id") },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            
            composable(
                route = "delivery_details/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val deliveryId = backStackEntry.arguments?.getString("id") ?: ""
                DeliveryDetailsScreen(
                    deliveryId = deliveryId,
                    viewModel = deliveryViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("inventory") {
                PhysicalInventoryScreen(
                    viewModel = inventoryViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            
            composable("returns") {
                ReturnsScreen(
                    viewModel = returnsViewModel,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            }
            
            composable("users") { 
                UserManagementScreen(
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    profileRepository = profileRepository
                ) 
            }
        }
    }
}
