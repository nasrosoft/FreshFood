package com.devsoft.freshfood.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devsoft.freshfood.domain.repository.ProfileRepository
import com.devsoft.freshfood.presentation.customers.AddCustomerScreen
import com.devsoft.freshfood.presentation.customers.CustomersScreen
import com.devsoft.freshfood.presentation.customers.CustomersViewModel
import com.devsoft.freshfood.presentation.dashboard.DashboardScreen
import com.devsoft.freshfood.presentation.dashboard.DashboardViewModel
import com.devsoft.freshfood.presentation.deliveries.DeliveryDashboardScreen
import com.devsoft.freshfood.presentation.deliveries.DeliveryDetailsScreen
import com.devsoft.freshfood.presentation.deliveries.DeliveryViewModel
import com.devsoft.freshfood.presentation.inventory.InventoryViewModel
import com.devsoft.freshfood.presentation.inventory.PhysicalInventoryScreen
import com.devsoft.freshfood.presentation.inventory.ReturnsScreen
import com.devsoft.freshfood.presentation.inventory.ReturnsViewModel
import com.devsoft.freshfood.presentation.products.AddProductScreen
import com.devsoft.freshfood.presentation.products.ProductListScreen
import com.devsoft.freshfood.presentation.products.ProductsViewModel
import com.devsoft.freshfood.presentation.purchases.PurchaseEntryScreen
import com.devsoft.freshfood.presentation.purchases.PurchaseViewModel
import com.devsoft.freshfood.presentation.sales.PosScreen
import com.devsoft.freshfood.presentation.sales.PosViewModel
import com.devsoft.freshfood.presentation.users.UserManagementScreen
import com.devsoft.freshfood.ui.theme.*
import com.devsoft.freshfood.utils.LocaleHelper
import kotlinx.coroutines.launch

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
    profileRepository: ProfileRepository,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: if (userRole == "DELIVERY") "deliveries" else "dashboard"
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDelivery = userRole == "DELIVERY"

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = CardSurface,
                modifier = Modifier.width(300.dp)
            ) {
                // Sidebar Brand Banner (from reference UI)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlueContainer)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🥛", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Fresh Dairy",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                "Stock & Sales Management",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // User Badge
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlueLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("👤", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    if (isDelivery) "Delivery Driver" else "Mohamed Admin",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    userRole,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Navigation Items
                if (!isDelivery) {
                    DrawerItem(
                        icon = Icons.Filled.Home,
                        label = "Dashboard",
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.ShoppingCart,
                        label = "Sales (POS)",
                        selected = currentRoute == "pos",
                        onClick = {
                            navController.navigate("pos") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.List,
                        label = "Products",
                        selected = currentRoute == "products",
                        onClick = {
                            navController.navigate("products") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Person,
                        label = "Customers",
                        selected = currentRoute == "customers",
                        onClick = {
                            navController.navigate("customers") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Add,
                        label = "Purchase Entry",
                        selected = currentRoute == "purchases",
                        onClick = {
                            navController.navigate("purchases") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Person,
                        label = "Users / Drivers",
                        selected = currentRoute == "users",
                        onClick = {
                            navController.navigate("users") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                DrawerItem(
                    icon = Icons.Filled.ShoppingCart,
                    label = "Deliveries",
                    selected = currentRoute == "deliveries",
                    onClick = {
                        navController.navigate("deliveries") { launchSingleTop = true; restoreState = true }
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = CardBorder)

                DrawerItem(
                    icon = Icons.Filled.Settings,
                    label = "Language / اللغة / Langue",
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLanguageDialog = true
                    }
                )

                DrawerItem(
                    icon = Icons.Filled.ExitToApp,
                    label = "Log out",
                    selected = false,
                    textColor = StatusError,
                    iconColor = StatusError,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onLogout()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    ) {
        if (showLanguageDialog) {
            val currentLang = LocaleHelper.getPersistedLanguage(context)
            val languages = listOf(
                Triple("ar", "العربية", "Arabic"),
                Triple("fr", "Français", "French"),
                Triple("en", "English", "English")
            )

            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Select Language / اختر اللغة", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        languages.forEach { (code, nativeName, engName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        LocaleHelper.setLocale(context, code)
                                        showLanguageDialog = false
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentLang == code,
                                    onClick = {
                                        LocaleHelper.setLocale(context, code)
                                        showLanguageDialog = false
                                        (context as? android.app.Activity)?.recreate()
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(nativeName, fontWeight = FontWeight.Bold)
                                    Text(engName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        Scaffold(
            bottomBar = {
                // Modern Bottom Navigation Bar matching reference UI
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = CardSurface
                ) {
                    if (isDelivery) {
                        NavigationBar(
                            containerColor = CardSurface,
                            contentColor = TextDark,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "deliveries",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Home") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "deliveries_list",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Deliveries") },
                                label = { Text("Deliveries") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { scope.launch { drawerState.open() } },
                                icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                                label = { Text("Profile") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                        }
                    } else {
                        NavigationBar(
                            containerColor = CardSurface,
                            contentColor = TextDark,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = currentRoute == "dashboard",
                                onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
                                label = { Text("Dashboard", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "products",
                                onClick = { navController.navigate("products") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.List, contentDescription = "Products") },
                                label = { Text("Products", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                            
                            // Center (+) Floating Action Button
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate("pos") { launchSingleTop = true; restoreState = true } },
                                icon = {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "New Sale", tint = Color.White)
                                    }
                                },
                                label = { Text("New Sale", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue) }
                            )

                            NavigationBarItem(
                                selected = currentRoute == "pos",
                                onClick = { navController.navigate("pos") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Sales") },
                                label = { Text("Sales", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                            NavigationBarItem(
                                selected = currentRoute == "deliveries",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                icon = { Icon(Icons.Filled.Menu, contentDescription = "More") },
                                label = { Text("Deliveries", fontSize = 11.sp) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PrimaryBlue,
                                    selectedTextColor = PrimaryBlue,
                                    indicatorColor = PrimaryBlueContainer
                                )
                            )
                        }
                    }
                }
            }
        ) { padding ->
            val startDest = if (isDelivery) "deliveries" else "dashboard"
            NavHost(
                navController = navController, 
                startDestination = startDest, 
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(AppBackground)
            ) {
                composable("dashboard") { 
                    DashboardScreen(
                        dashboardViewModel = dashboardViewModel,
                        onOpenDrawer = { scope.launch { drawerState.open() } },
                        onNavigateToSales = { navController.navigate("pos") },
                        onNavigateToDeliveries = { navController.navigate("deliveries") }
                    ) 
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
                    "add_product?barcode={barcode}",
                    arguments = listOf(navArgument("barcode") { type = NavType.StringType; nullable = true })
                ) { backStackEntry ->
                    val barcode = backStackEntry.arguments?.getString("barcode")
                    AddProductScreen(
                        viewModel = productsViewModel,
                        initialBarcode = barcode,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("customers") { 
                    CustomersScreen(
                        viewModel = customersViewModel, 
                        onAddCustomerClick = { navController.navigate("add_customer") },
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    ) 
                }
                composable("add_customer") { 
                    AddCustomerScreen(
                        viewModel = customersViewModel,
                        onBack = { navController.popBackStack() }
                    ) 
                }
                composable("purchases") { 
                    PurchaseEntryScreen(
                        viewModel = purchaseViewModel,
                        productsViewModel = productsViewModel,
                        onBack = { navController.popBackStack() }
                    ) 
                }
                composable("deliveries") { 
                    DeliveryDashboardScreen(
                        viewModel = deliveryViewModel,
                        currentUserId = userId,
                        currentUserRole = userRole,
                        onDeliveryClick = { deliveryId ->
                            navController.navigate("delivery_details/$deliveryId")
                        },
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    ) 
                }
                composable(
                    "delivery_details/{deliveryId}",
                    arguments = listOf(navArgument("deliveryId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val deliveryId = backStackEntry.arguments?.getString("deliveryId") ?: ""
                    DeliveryDetailsScreen(
                        deliveryId = deliveryId,
                        viewModel = deliveryViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("users") {
                    UserManagementScreen(
                        profileRepository = profileRepository,
                        onOpenDrawer = { scope.launch { drawerState.open() } }
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    textColor: Color = TextDark,
    iconColor: Color = PrimaryBlue,
    onClick: () -> Unit
) {
    NavigationDrawerItem(
        icon = { Icon(icon, contentDescription = null, tint = if (selected) PrimaryBlue else iconColor) },
        label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = if (selected) PrimaryBlue else textColor) },
        selected = selected,
        onClick = onClick,
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = PrimaryBlueContainer,
            unselectedContainerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
    )
}
