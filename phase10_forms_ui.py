import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\devsoft\freshfood'
ui_theme_dir = os.path.join(base_dir, 'ui', 'theme')
products_dir = os.path.join(base_dir, 'presentation', 'products')
customers_dir = os.path.join(base_dir, 'presentation', 'customers')
nav_dir = os.path.join(base_dir, 'presentation', 'navigation')

os.makedirs(ui_theme_dir, exist_ok=True)
os.makedirs(products_dir, exist_ok=True)
os.makedirs(customers_dir, exist_ok=True)
os.makedirs(nav_dir, exist_ok=True)

files = {
    # 1. Premium UI: Color.kt
    os.path.join(ui_theme_dir, 'Color.kt'): '''package com.devsoft.freshfood.ui.theme

import androidx.compose.ui.graphics.Color

val PrimaryGreen = Color(0xFF2E7D32)
val PrimaryGreenLight = Color(0xFF60AD5E)
val PrimaryGreenDark = Color(0xFF005005)

val SecondaryBlue = Color(0xFF1565C0)
val SecondaryBlueLight = Color(0xFF5E92F3)
val SecondaryBlueDark = Color(0xFF003C8F)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)

val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF388E3C)
val WarningOrange = Color(0xFFF57C00)

val TextPrimary = Color(0xFF1E293B)
val TextSecondary = Color(0xFF64748B)
''',

    # 2. Premium UI: Theme.kt
    os.path.join(ui_theme_dir, 'Theme.kt'): '''package com.devsoft.freshfood.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryBlue,
    tertiary = PrimaryGreenLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun FreshFoodTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme // Force light theme for consistency in fresh food app
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
''',

    # 3. Add Product Screen
    os.path.join(products_dir, 'AddProductScreen.kt'): '''package com.devsoft.freshfood.presentation.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf("") }
    var sellPrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var currentStock by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Product") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("Barcode (SKU)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = sellPrice, onValueChange = { sellPrice = it }, label = { Text("Selling Price (DA)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = costPrice, onValueChange = { costPrice = it }, label = { Text("Cost Price (DA)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = currentStock, onValueChange = { currentStock = it }, label = { Text("Initial Stock Quantity") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Product")
            }
        }
    }
}
''',

    # 4. Add Customer Screen
    os.path.join(customers_dir, 'AddCustomerScreen.kt'): '''package com.devsoft.freshfood.presentation.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerScreen(
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("") }
    var isWholesale by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Customer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Customer Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = creditLimit, onValueChange = { creditLimit = it }, label = { Text("Credit Limit (DA)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isWholesale, onCheckedChange = { isWholesale = it })
                Text("Wholesale Customer (Grossiste)")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onBack() },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Customer")
            }
        }
    }
}
''',

    # 5. Overwrite MainAppScreen.kt to add routes
    os.path.join(nav_dir, 'MainAppScreen.kt'): '''package com.devsoft.freshfood.presentation.navigation

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
'''
}

for path, content in files.items():
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Phase 10 Forms and UI Script Executed Successfully.")
