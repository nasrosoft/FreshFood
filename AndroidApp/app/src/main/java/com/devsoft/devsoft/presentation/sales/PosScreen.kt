package com.devsoft.devsoft.presentation.sales

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
import androidx.compose.ui.res.stringResource
import com.devsoft.devsoft.R
import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.domain.model.Product
import com.devsoft.devsoft.domain.model.Profile
import com.devsoft.devsoft.presentation.components.BarcodeScannerScreen
import com.devsoft.devsoft.presentation.components.ProductImageView
import com.devsoft.devsoft.presentation.customers.CustomersUiState
import com.devsoft.devsoft.presentation.customers.CustomersViewModel
import com.devsoft.devsoft.presentation.products.ProductsViewModel
import com.devsoft.devsoft.ui.theme.*
import com.devsoft.devsoft.utils.PdfReceiptGenerator

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
        BarcodeScannerScreen(
            onBarcodeScanned = { barcode ->
                showScanner = false
                val product = productsViewModel.findProductByBarcode(barcode)
                if (product != null) {
                    viewModel.addToCart(product)
                }
            },
            onDismiss = { showScanner = false }
        )
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

    LaunchedEffect(uiState.checkoutMessage) {
        if (uiState.checkoutMessage?.startsWith("Success") == true && createDelivery) {
            val orderId = uiState.lastSaleId
            val shortId = orderId?.take(8)?.uppercase() ?: ""
            com.devsoft.devsoft.utils.NotificationHelper.showDeliveryNotification(
                context = context,
                title = "Nouvelle livraison assignée 🚚",
                message = "Commande ${if (shortId.isNotBlank()) "#$shortId " else ""}de ${selectedCustomer?.name ?: "Client"} prête pour la livraison.",
                orderId = orderId
            )
        }
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
                    val finalDriverName = uiState.lastDriverName ?: (if (selectedDriver != null) {
                        listOfNotNull(selectedDriver?.first_name, selectedDriver?.last_name).joinToString(" ").ifBlank { selectedDriver?.email }
                    } else null)
                    val isDeliveryOrder = !finalDriverName.isNullOrBlank()

                    Text(if (isDeliveryOrder) "Order Created" else "Invoice", fontWeight = FontWeight.Bold)
                    Text(
                        if (isDeliveryOrder) "🚚 Pending Delivery" else "✓ Completed", 
                        color = if (isDeliveryOrder) PrimaryBlue else StatusSuccess, 
                        fontSize = 13.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val finalCustomerName = uiState.lastCustomerName ?: selectedCustomer?.name ?: "Guest"
                    val finalPaymentMethod = uiState.lastPaymentMethod ?: selectedPaymentMethod
                    val finalDriverName = uiState.lastDriverName ?: (if (selectedDriver != null) {
                        listOfNotNull(selectedDriver?.first_name, selectedDriver?.last_name).joinToString(" ").ifBlank { selectedDriver?.email }
                    } else null)
                    val isDeliveryOrder = !finalDriverName.isNullOrBlank()

                    Text("Customer: $finalCustomerName", fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("Payment Method: $finalPaymentMethod", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    if (!finalDriverName.isNullOrBlank()) {
                        Text("Driver: $finalDriverName", style = MaterialTheme.typography.bodySmall, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = CardBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(items) { cartItem ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "${cartItem.quantity}x ${cartItem.product.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                                )
                                Text(
                                    "${String.format(java.util.Locale.US, "%,.1f", cartItem.quantity * cartItem.product.selling_price)} DA",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue,
                                    softWrap = false
                                )
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

                    val creditVal = uiState.lastCustomerCredit ?: selectedCustomer?.current_credit
                    if (creditVal != null && creditVal > 0) {
                        val prevCred = creditVal
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isDeliveryOrder) {
                            Text(
                                "Client Current Credit: ${String.format(java.util.Locale.US, "%,.0f", prevCred)} DA",
                                color = StatusWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            val isCredit = finalPaymentMethod == "CREDIT"
                            val newCred = prevCred + (if (isCredit) total else 0.0)
                            Text(
                                "Total Client Credit: ${String.format(java.util.Locale.US, "%,.0f", newCred)} DA",
                                color = StatusWarning,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
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
                    val finalCustomerName = uiState.lastCustomerName ?: selectedCustomer?.name ?: "Guest"
                    val finalPaymentMethod = uiState.lastPaymentMethod ?: selectedPaymentMethod
                    val finalDriverName = uiState.lastDriverName ?: (if (selectedDriver != null) {
                        listOfNotNull(selectedDriver?.first_name, selectedDriver?.last_name).joinToString(" ").ifBlank { selectedDriver?.email }
                    } else null)

                    val isDeliveryOrder = uiState.lastIsDelivery ?: createDelivery
                    val finalOrderStatus = if (isDeliveryOrder) {
                        if (finalDriverName != null) "ASSIGNED" else "PENDING"
                    } else null

                    val uri = PdfReceiptGenerator.generateAndGetUri(
                        context = context,
                        cartItems = items,
                        totalAmount = total,
                        customerName = finalCustomerName,
                        driverName = finalDriverName,
                        paymentMethod = finalPaymentMethod,
                        orderId = uiState.lastSaleId,
                        customerCurrentCredit = uiState.lastCustomerCredit ?: selectedCustomer?.current_credit,
                        customerCreditLimit = uiState.lastCustomerCreditLimit ?: selectedCustomer?.credit_limit,
                        orderStatus = finalOrderStatus
                    )
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
                            Text(stringResource(R.string.new_sale), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                        }
                        com.devsoft.devsoft.presentation.components.GlobalSyncButton()
                    }

                    // Search & Barcode Top Bar (matching reference UI)
                    OutlinedTextField(
                        value = productsState.searchQuery,
                        onValueChange = { productsViewModel.updateSearchQuery(it) },
                        placeholder = { Text(stringResource(R.string.scan_or_search), color = TextMuted, fontSize = 14.sp) },
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
                            selectedCustomer?.name ?: stringResource(R.string.select_customer), 
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
                            if (createDelivery) (selectedDriver?.first_name ?: stringResource(R.string.assign_driver)) else stringResource(R.string.delivery),
                            fontSize = 12.sp
                        ) 
                    },
                    leadingIcon = { Icon(Icons.Filled.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            // Quick Add Horizontal List (matching reference design)
            Text(
                stringResource(R.string.quick_add),
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
                    val isLowStock = product.current_stock <= product.min_stock
                    val inCartQty = uiState.cartItems.find { it.product.id == product.id }?.quantity ?: 0
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .width(136.dp)
                            .clickable { viewModel.addToCart(product) }
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
                            Spacer(modifier = Modifier.height(4.dp))
                            // Available Stock Quantity Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isLowStock) StatusWarningContainer else StatusSuccessContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.stock_label, product.current_stock),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isLowStock) StatusWarning else StatusSuccess
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (inCartQty > 0) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(PrimaryBlueContainer)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.in_cart, inCartQty),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(PrimaryBlue),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                                }
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
                        Text(stringResource(R.string.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                        Text("${String.format(java.util.Locale.US, "%,.0f", total)} DA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { showPaymentMethodDialog = true },
                        enabled = uiState.cartItems.isNotEmpty() && (!createDelivery || selectedDriver != null),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text(stringResource(R.string.proceed_to_payment), fontWeight = FontWeight.Bold)
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
            title = { Text(stringResource(R.string.select_payment_method), fontWeight = FontWeight.Bold) },
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
                        Text(stringResource(R.string.cash_payment), fontWeight = FontWeight.Bold)
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
                        Text(stringResource(R.string.credit_payment), fontWeight = FontWeight.Bold, color = StatusWarning)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showPaymentMethodDialog = false
                    val driverFullName = if (selectedDriver != null) {
                        listOfNotNull(selectedDriver?.first_name, selectedDriver?.last_name).joinToString(" ").ifBlank { selectedDriver?.email }
                    } else null

                    viewModel.checkout(
                        paymentMethod = selectedPaymentMethod,
                        customerId = selectedCustomer?.id,
                        customerName = selectedCustomer?.name ?: "Guest",
                        customerCredit = selectedCustomer?.current_credit,
                        customerCreditLimit = selectedCustomer?.credit_limit,
                        createDelivery = createDelivery,
                        deliveryDriverId = selectedDriver?.id,
                        deliveryDriverName = driverFullName
                    )
                }) {
                    Text(stringResource(R.string.confirm_and_checkout))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentMethodDialog = false }) { 
                    Text(stringResource(R.string.cancel)) 
                }
            }
        )
    }

    // Customer Selection Dialog
    if (showCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text(stringResource(R.string.select_customer), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerSearchQuery,
                        onValueChange = { customerSearchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        placeholder = { Text(stringResource(R.string.search_customers_placeholder)) },
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
                                Text(stringResource(R.string.guest_customer))
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
                                            Text(stringResource(R.string.credit_badge, String.format(java.util.Locale.US, "%,.0f", cust.current_credit)), fontSize = 11.sp, color = StatusWarning)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerDialog = false }) { 
                    Text(stringResource(R.string.close)) 
                }
            }
        )
    }

    // Driver Selection Dialog
    if (showDriverDialog) {
        AlertDialog(
            onDismissRequest = { showDriverDialog = false },
            title = { Text(stringResource(R.string.assign_driver_title), fontWeight = FontWeight.Bold) },
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
                TextButton(onClick = { showDriverDialog = false }) { 
                    Text(stringResource(R.string.close)) 
                }
            }
        )
    }
}
