package com.devsoft.freshfood.presentation.sales

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.Profile
import com.devsoft.freshfood.presentation.components.BarcodeScannerScreen
import com.devsoft.freshfood.presentation.components.ProductImageView
import com.devsoft.freshfood.presentation.customers.CustomersUiState
import com.devsoft.freshfood.presentation.customers.CustomersViewModel
import com.devsoft.freshfood.presentation.products.ProductsViewModel
import com.devsoft.freshfood.ui.theme.*
import com.devsoft.freshfood.utils.PdfReceiptGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    productsViewModel: ProductsViewModel,
    customersViewModel: CustomersViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val productsState by productsViewModel.uiState.collectAsState()
    val customersState by customersViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerSearchQuery by remember { mutableStateOf("") }
    
    var showDriverDialog by remember { mutableStateOf(false) }
    var selectedDriver by remember { mutableStateOf<Profile?>(null) }
    var createDelivery by remember { mutableStateOf(false) }
    var showScanner by remember { mutableStateOf(false) }

    var showPaymentMethodDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("CASH") }

    var quantityDialogItem by remember { mutableStateOf<CartItem?>(null) }
    var customQuantityText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        customersViewModel.loadCustomers()
    }

    if (showScanner) {
        BarcodeScannerScreen(onBarcodeScanned = { barcode ->
            showScanner = false
            val product = productsViewModel.findProductByBarcode(barcode)
            if (product != null) {
                viewModel.addToCart(product)
            }
        })
        return
    }

    if (quantityDialogItem != null) {
        AlertDialog(
            onDismissRequest = { quantityDialogItem = null },
            title = { Text("Set Quantity", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = customQuantityText,
                    onValueChange = { customQuantityText = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Quantity") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val qty = customQuantityText.toIntOrNull()
                    if (qty != null) {
                        viewModel.setQuantity(quantityDialogItem!!.product, qty)
                    }
                    quantityDialogItem = null
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { quantityDialogItem = null }) { Text("Cancel") }
            }
        )
    }

    // Modern Invoice Result Dialog
    if (uiState.checkoutMessage != null) {
        val total = uiState.lastSaleTotal ?: 0.0
        val items = uiState.lastSaleItems ?: emptyList()

        AlertDialog(
            onDismissRequest = { viewModel.dismissCheckoutMessage() },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Invoice", fontWeight = FontWeight.Bold)
                    Text("✓ Completed", color = StatusSuccess, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Customer: ${selectedCustomer?.name ?: "Guest"}", fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("Payment Method: $selectedPaymentMethod", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(items) { cartItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${cartItem.quantity}x ${cartItem.product.name}", style = MaterialTheme.typography.bodyMedium, color = TextDark)
                                Text("${cartItem.quantity * cartItem.product.selling_price} DA", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("${String.format(java.util.Locale.US, "%,.0f", total)} DA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = PrimaryBlueDark)
                    }
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.dismissCheckoutMessage()
                    selectedCustomer = null
                    selectedDriver = null
                    createDelivery = false
                }) {
                    Text("New Sale")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    val uri = PdfReceiptGenerator.generateAndGetUri(context, items, total)
                    if (uri != null) {
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(android.content.Intent.createChooser(intent, "Print / Share Receipt"))
                    }
                }) {
                    Text("Print / Share PDF")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PrimaryBlueDark)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Sale", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                    }

                    // Search & Barcode Top Bar (matching reference UI)
                    OutlinedTextField(
                        value = productsState.searchQuery,
                        onValueChange = { productsViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Scan barcode or search product...", color = TextMuted, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                        trailingIcon = {
                            IconButton(onClick = { showScanner = true }) {
                                Icon(Icons.Filled.List, contentDescription = "Scan", tint = PrimaryBlue)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardSurfaceVariant,
                            unfocusedContainerColor = CardSurfaceVariant,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Customer & Delivery Selection Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Customer Chip
                AssistChip(
                    onClick = { showCustomerDialog = true },
                    label = { 
                        Text(
                            selectedCustomer?.name ?: "Select Customer", 
                            fontSize = 12.sp,
                            fontWeight = if (selectedCustomer != null) FontWeight.Bold else FontWeight.Normal
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )

                // Delivery Toggle Chip
                FilterChip(
                    selected = createDelivery,
                    onClick = { 
                        createDelivery = !createDelivery
                        if (createDelivery) showDriverDialog = true
                    },
                    label = { 
                        Text(
                            if (createDelivery) (selectedDriver?.first_name ?: "Assign Driver") else "Delivery",
                            fontSize = 12.sp
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Quick Add Horizontal List (matching reference design)
            Text(
                "Quick Add",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(productsState.filteredProducts) { product ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.width(130.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProductImageView(
                                imageUrl = product.image_url,
                                emoji = product.emoji,
                                productName = product.name,
                                size = 48.dp,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                product.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${product.selling_price} DA",
                                fontSize = 11.sp,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue)
                                    .clickable { viewModel.addToCart(product) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Cart Items List
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp).fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cart (${uiState.cartItems.sumOf { it.quantity }})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        if (uiState.cartItems.isNotEmpty()) {
                            Text(
                                "Clear",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusError,
                                modifier = Modifier.clickable { viewModel.clearCart() }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.cartItems.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Your cart is empty. Add products above.", color = TextMuted, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(uiState.cartItems) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ProductImageView(
                                        imageUrl = item.product.image_url,
                                        emoji = item.product.emoji,
                                        productName = item.product.name,
                                        size = 40.dp,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${item.quantity} x ${item.product.selling_price} DA", fontSize = 11.sp, color = TextMuted)
                                    }

                                    // Quantity Stepper [- 5 +]
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardSurfaceVariant)
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.decreaseQuantity(item.product) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryBlue)
                                        }
                                        Text(
                                            "${item.quantity}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            modifier = Modifier
                                                .padding(horizontal = 4.dp)
                                                .clickable { 
                                                    quantityDialogItem = item
                                                    customQuantityText = item.quantity.toString()
                                                }
                                        )
                                        IconButton(
                                            onClick = { viewModel.addToCart(item.product) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorder)

                    // Subtotal & Total
                    val total = uiState.totalAmount
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("${String.format(java.util.Locale.US, "%,.0f", total)} DA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showPaymentMethodDialog = true },
                        enabled = uiState.cartItems.isNotEmpty() && (!createDelivery || selectedDriver != null),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Proceed to Payment", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    // Payment Method Selection Dialog
    if (showPaymentMethodDialog) {
        AlertDialog(
            onDismissRequest = { showPaymentMethodDialog = false },
            title = { Text("Select Payment Method", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "CASH" }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPaymentMethod == "CASH", onClick = { selectedPaymentMethod = "CASH" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cash (Espèces)", fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "CREDIT" }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPaymentMethod == "CREDIT", onClick = { selectedPaymentMethod = "CREDIT" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Customer Credit (Crédit)", fontWeight = FontWeight.Bold, color = StatusWarning)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPaymentMethodDialog = false
                    viewModel.checkout(selectedPaymentMethod, selectedCustomer?.id, createDelivery, selectedDriver?.id)
                }) {
                    Text("Confirm & Checkout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentMethodDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Select Customer", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerSearchQuery,
                        onValueChange = { customerSearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text("Search by name...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                        singleLine = true
                    )
                    
                    LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                        item {
                            TextButton(
                                onClick = {
                                    selectedCustomer = null
                                    showCustomerDialog = false
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guest (No Customer)")
                            }
                        }
                        if (customersState is CustomersUiState.Success) {
                            val allCustomers = (customersState as CustomersUiState.Success).customers
                            val filtered = if (customerSearchQuery.isBlank()) allCustomers else allCustomers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }
                            items(filtered) { cust ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable {
                                            selectedCustomer = cust
                                            showCustomerDialog = false
                                        },
                                    colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        if (cust.current_credit > 0) {
                                            Text("Credit: ${cust.current_credit} DA", fontSize = 11.sp, color = StatusWarning)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerDialog = false }) { Text("Close") }
            }
        )
    }

    // Driver Selection Dialog
    if (showDriverDialog) {
        AlertDialog(
            onDismissRequest = { showDriverDialog = false },
            title = { Text("Assign Delivery Driver", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                    items(uiState.deliveryDrivers) { driver: Profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    selectedDriver = driver
                                    showDriverDialog = false
                                },
                            colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant)
                        ) {
                            Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = null, tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    listOfNotNull(driver.first_name, driver.last_name).joinToString(" ").ifBlank { driver.email ?: "Driver" },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDriverDialog = false }) { Text("Close") }
            }
        )
    }
}
