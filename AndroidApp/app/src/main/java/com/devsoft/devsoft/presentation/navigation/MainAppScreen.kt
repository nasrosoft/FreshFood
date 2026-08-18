package com.devsoft.devsoft.presentation.navigation

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.devsoft.R
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devsoft.devsoft.domain.model.AppSetting
import com.devsoft.devsoft.domain.model.Profile
import com.devsoft.devsoft.domain.repository.ActivationRepository
import com.devsoft.devsoft.domain.repository.ProfileRepository
import com.devsoft.devsoft.presentation.customers.AddCustomerScreen
import com.devsoft.devsoft.presentation.customers.CustomersScreen
import com.devsoft.devsoft.presentation.customers.CustomersViewModel
import com.devsoft.devsoft.presentation.dashboard.DashboardScreen
import com.devsoft.devsoft.presentation.dashboard.DashboardViewModel
import com.devsoft.devsoft.presentation.deliveries.DeliveryDashboardScreen
import com.devsoft.devsoft.presentation.deliveries.DeliveryDetailsScreen
import com.devsoft.devsoft.presentation.deliveries.DeliveryViewModel
import com.devsoft.devsoft.presentation.inventory.InventoryViewModel
import com.devsoft.devsoft.presentation.inventory.PhysicalInventoryScreen
import com.devsoft.devsoft.presentation.inventory.ReturnsScreen
import com.devsoft.devsoft.presentation.inventory.ReturnsViewModel
import com.devsoft.devsoft.presentation.products.AddProductScreen
import com.devsoft.devsoft.presentation.products.ProductListScreen
import com.devsoft.devsoft.presentation.products.ProductsViewModel
import com.devsoft.devsoft.presentation.purchases.PurchaseEntryScreen
import com.devsoft.devsoft.presentation.purchases.PurchaseViewModel
import com.devsoft.devsoft.presentation.sales.PosScreen
import com.devsoft.devsoft.presentation.sales.PosViewModel
import com.devsoft.devsoft.presentation.users.UserManagementScreen
import com.devsoft.devsoft.ui.theme.*
import com.devsoft.devsoft.utils.LocaleHelper
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
    activationRepository: ActivationRepository? = null,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: if (userRole == "DELIVERY") "deliveries" else "dashboard"
    val context = androidx.compose.ui.platform.LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showParametrageDialog by remember { mutableStateOf(false) }

    // Dynamic brand settings from Supabase
    var brandName by remember { mutableStateOf("DevSoft") }
    var brandTagline by remember { mutableStateOf("Stock & Sales Management") }
    var editBrandName by remember { mutableStateOf("DevSoft") }
    var editBrandTagline by remember { mutableStateOf("Stock & Sales Management") }
    var isSavingBrand by remember { mutableStateOf(false) }

    // Dynamic user profile from Supabase
    var userProfile by remember { mutableStateOf<Profile?>(null) }

    LaunchedEffect(userId) {
        if (!userId.isNullOrBlank()) {
            try {
                userProfile = profileRepository.getProfileById(userId)
            } catch (e: Exception) {
                // Ignore cancellation exceptions on activity recreate
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            activationRepository?.getAppSettings()?.onSuccess { setting: AppSetting ->
                val fetchedName = setting.brand_name?.ifBlank { "DevSoft" } ?: "DevSoft"
                val fetchedTagline = setting.brand_tagline?.ifBlank { "Stock & Sales Management" } ?: "Stock & Sales Management"
                brandName = fetchedName
                brandTagline = fetchedTagline
                editBrandName = fetchedName
                editBrandTagline = fetchedTagline
            }
        } catch (e: Exception) {
            // Ignore cancellation exceptions on activity recreate
        }
    }

    val userDisplayName = remember(userProfile, userRole) {
        if (userProfile != null) {
            listOfNotNull(userProfile?.first_name, userProfile?.last_name)
                .joinToString(" ")
                .ifBlank { userProfile?.email ?: (if (userRole == "DELIVERY") "Delivery Driver" else "Administrator") }
        } else {
            if (userRole == "DELIVERY") "Delivery Driver" else "Administrator"
        }
    }

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
                // Sidebar Brand Banner (from Supabase)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryBlueContainer)
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                brandName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                brandTagline,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Real User Badge from Supabase
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
                                    userDisplayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    stringResource(if (isDelivery) R.string.delivery_role else R.string.admin_role),
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
                        label = stringResource(R.string.dashboard),
                        selected = currentRoute == "dashboard",
                        onClick = {
                            navController.navigate("dashboard") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.ShoppingCart,
                        label = stringResource(R.string.sales_pos),
                        selected = currentRoute == "pos",
                        onClick = {
                            navController.navigate("pos") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.List,
                        label = stringResource(R.string.products),
                        selected = currentRoute == "products",
                        onClick = {
                            navController.navigate("products") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Person,
                        label = stringResource(R.string.customers),
                        selected = currentRoute == "customers",
                        onClick = {
                            navController.navigate("customers") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Add,
                        label = stringResource(R.string.purchase_entry),
                        selected = currentRoute == "purchases",
                        onClick = {
                            navController.navigate("purchases") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                    DrawerItem(
                        icon = Icons.Filled.Person,
                        label = stringResource(R.string.users_drivers),
                        selected = currentRoute == "users",
                        onClick = {
                            navController.navigate("users") { launchSingleTop = true; restoreState = true }
                            scope.launch { drawerState.close() }
                        }
                    )
                }

                DrawerItem(
                    icon = Icons.Filled.ShoppingCart,
                    label = stringResource(R.string.deliveries),
                    selected = currentRoute == "deliveries",
                    onClick = {
                        navController.navigate("deliveries") { launchSingleTop = true; restoreState = true }
                        scope.launch { drawerState.close() }
                    }
                )

                Spacer(modifier = Modifier.weight(1f))
                Divider(modifier = Modifier.padding(horizontal = 16.dp), color = CardBorder)

                // Paramétrage Button
                DrawerItem(
                    icon = Icons.Filled.Build,
                    label = stringResource(R.string.parametrage),
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        editBrandName = brandName
                        editBrandTagline = brandTagline
                        showParametrageDialog = true
                    }
                )

                DrawerItem(
                    icon = Icons.Filled.Settings,
                    label = stringResource(R.string.language),
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        showLanguageDialog = true
                    }
                )

                DrawerItem(
                    icon = Icons.Filled.ExitToApp,
                    label = stringResource(R.string.logout),
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
        // Paramétrage Modal Dialog
        if (showParametrageDialog) {
            AlertDialog(
                onDismissRequest = { if (!isSavingBrand) showParametrageDialog = false },
                title = { Text("Paramétrage / Brand Settings", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("Customize your store or brand identity:", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = editBrandName,
                            onValueChange = { editBrandName = it },
                            label = { Text("Brand Name / Nom Commercial") },
                            placeholder = { Text("e.g. DevSoft") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editBrandTagline,
                            onValueChange = { editBrandTagline = it },
                            label = { Text("Tagline / Description") },
                            placeholder = { Text("e.g. Stock & Sales Management") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editBrandName.isNotBlank()) {
                                isSavingBrand = true
                                scope.launch {
                                    activationRepository?.updateBrandSettings(editBrandName.trim(), editBrandTagline.trim())?.fold(
                                        onSuccess = {
                                            brandName = editBrandName.trim()
                                            brandTagline = editBrandTagline.trim()
                                            isSavingBrand = false
                                            showParametrageDialog = false
                                            android.widget.Toast.makeText(context, "Brand updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        onFailure = { error: Throwable ->
                                            isSavingBrand = false
                                            android.widget.Toast.makeText(context, "Failed to update: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            }
                        },
                        enabled = !isSavingBrand && editBrandName.isNotBlank(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSavingBrand) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Saving...")
                        } else {
                            Text("Save to Supabase")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showParametrageDialog = false }, enabled = !isSavingBrand) {
                        Text("Cancel")
                    }
                }
            )
        }

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
                // Responsive Modern Bottom Navigation Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = CardSurface
                ) {
                    if (isDelivery) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(60.dp)
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavButton(
                                icon = Icons.Filled.Home,
                                label = stringResource(R.string.home),
                                selected = currentRoute == "deliveries",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
                            )
                            BottomNavButton(
                                icon = Icons.Filled.ShoppingCart,
                                label = stringResource(R.string.deliveries),
                                selected = currentRoute == "deliveries_list",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
                            )
                            BottomNavButton(
                                icon = Icons.Filled.Person,
                                label = stringResource(R.string.profile),
                                selected = false,
                                onClick = { scope.launch { drawerState.open() } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(62.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BottomNavButton(
                                icon = Icons.Filled.Home,
                                label = stringResource(R.string.dashboard),
                                selected = currentRoute == "dashboard",
                                onClick = { navController.navigate("dashboard") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
                            )
                            BottomNavButton(
                                icon = Icons.Filled.List,
                                label = stringResource(R.string.products),
                                selected = currentRoute == "products",
                                onClick = { navController.navigate("products") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
                            )
                            
                            // Center (+) Elevated FAB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { navController.navigate("pos") { launchSingleTop = true; restoreState = true } },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Add, 
                                        contentDescription = stringResource(R.string.new_sale), 
                                        tint = Color.White, 
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            BottomNavButton(
                                icon = Icons.Filled.ShoppingCart,
                                label = stringResource(R.string.sales),
                                selected = currentRoute == "pos",
                                onClick = { navController.navigate("pos") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
                            )
                            BottomNavButton(
                                icon = Icons.Filled.Menu,
                                label = stringResource(R.string.deliveries),
                                selected = currentRoute == "deliveries",
                                onClick = { navController.navigate("deliveries") { launchSingleTop = true; restoreState = true } },
                                modifier = Modifier.weight(1f)
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

@Composable
private fun BottomNavButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) PrimaryBlueContainer else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (selected) PrimaryBlue else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) PrimaryBlue else TextMuted,
            maxLines = 1,
            softWrap = false
        )
    }
}

