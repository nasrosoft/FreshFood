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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.products.ProductsViewModel

// Custom colors based on the image
val PosGreen = Color(0xFF679B50)
val PosLightGreen = Color(0xFFE8F3E5)
val PosBackground = Color(0xFFF3F3F3)
val PosRed = Color(0xFFE5695C)
val PosDarkGrey = Color(0xFF333333)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel,
    productsViewModel: ProductsViewModel,
    customersViewModel: com.devsoft.freshfood.presentation.customers.CustomersViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val productsState by productsViewModel.uiState.collectAsState()
    val customersState by customersViewModel.uiState.collectAsState()
    
    var showCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomer by remember { mutableStateOf<com.devsoft.freshfood.domain.model.Customer?>(null) }
    var customerSearchQuery by remember { mutableStateOf("") }
    
    // Load customers when screen opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        customersViewModel.loadCustomers()
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current

    var showScanner by remember { mutableStateOf(false) }
    var quantityDialogItem by remember { mutableStateOf<com.devsoft.freshfood.presentation.sales.CartItem?>(null) }
    var customQuantityText by remember { mutableStateOf("") }
    var createDelivery by remember { mutableStateOf(false) }

    if (quantityDialogItem != null) {
        AlertDialog(
            onDismissRequest = { quantityDialogItem = null },
            title = { Text("Set Quantity") },
            text = {
                OutlinedTextField(
                    value = customQuantityText,
                    onValueChange = { customQuantityText = it },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    singleLine = true,
                    label = { Text("Quantity") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
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
                TextButton(onClick = { quantityDialogItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showScanner) {
        com.devsoft.freshfood.presentation.components.BarcodeScannerScreen(onBarcodeScanned = { barcode ->
            showScanner = false
            val product = productsViewModel.findProductByBarcode(barcode)
            if (product != null) {
                viewModel.addToCart(product)
            }
        })
        return
    }

    if (uiState.checkoutMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCheckoutMessage() },
            title = { Text("Checkout") },
            text = { Text(uiState.checkoutMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissCheckoutMessage() }) {
                    Text("OK")
                }
            },
            dismissButton = {
                if (uiState.lastSaleItems != null && uiState.lastSaleTotal != null) {
                    TextButton(onClick = {
                        val uri = com.devsoft.freshfood.utils.PdfReceiptGenerator.generateAndGetUri(
                            context,
                            uiState.lastSaleItems!!,
                            uiState.lastSaleTotal!!
                        )
                        if (uri != null) {
                            val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(android.content.Intent.createChooser(intent, "Share Receipt"))
                        }
                    }) {
                        Text("Print / Share")
                    }
                }
            }
        )
    }

    if (showCustomerDialog) {
        AlertDialog(
            onDismissRequest = { showCustomerDialog = false },
            title = { Text("Select Customer") },
            text = {
                Column {
                    OutlinedTextField(
                        value = customerSearchQuery,
                        onValueChange = { customerSearchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        placeholder = { Text("Search by name...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true
                    )
                    
                    LazyColumn {
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
                        if (customersState is com.devsoft.freshfood.presentation.customers.CustomersUiState.Success) {
                            val allCustomers = (customersState as com.devsoft.freshfood.presentation.customers.CustomersUiState.Success).customers
                            val filteredCustomers = if (customerSearchQuery.isBlank()) {
                                allCustomers
                            } else {
                                allCustomers.filter { it.name.contains(customerSearchQuery, ignoreCase = true) }
                            }
                            
                            items(filteredCustomers) { customer ->
                                TextButton(
                                    onClick = {
                                        selectedCustomer = customer
                                        showCustomerDialog = false
                                        customerSearchQuery = "" // Reset query
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(customer.name)
                                }
                            }
                        } else if (customersState is com.devsoft.freshfood.presentation.customers.CustomersUiState.Loading) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCustomerDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill", fontWeight = FontWeight.Bold, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black)
                    }
                },
                actions = {
                    com.devsoft.freshfood.presentation.components.GlobalSyncButton(tint = Color.Black)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = PosBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // 1. Select Customer (Dashed border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PosLightGreen)
                    .clickable { showCustomerDialog = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = PosGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedCustomer?.name ?: "Select Customer", 
                        color = PosGreen, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // 2. Search Bar
            OutlinedTextField(
                value = productsState.searchQuery,
                onValueChange = { productsViewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products by name or SKU", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.List, // Placeholder for Barcode scanner icon
                        contentDescription = "Scan",
                        tint = Color.Black,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .clickable { showScanner = true }
                            .padding(4.dp)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color.LightGray,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )



            // 4. Products Horizontal List
            if (productsState.isLoading) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = PosGreen)
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(productsState.filteredProducts.size) { index ->
                        val product = productsState.filteredProducts[index]
                        ProductPosCard(product = product, onClick = { viewModel.addToCart(product) })
                    }
                }
            }

            // 5. Bill Items Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bill Items (${uiState.cartItems.size})",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosDarkGrey
                )
                Text(
                    text = "Total: ${uiState.totalAmount} DA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = PosGreen
                )
            }

            // 6. Bill Items List (Scrollable)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.cartItems.size) { index ->
                    val item = uiState.cartItems[index]
                    CartItemRow(
                        item = item,
                        onIncrease = { viewModel.addToCart(item.product) },
                        onDecrease = { viewModel.decreaseQuantity(item.product) },
                        onRemove = { viewModel.removeItem(item.product) },
                        onQuantityClick = { 
                            quantityDialogItem = item
                            customQuantityText = item.quantity.toString()
                        }
                    )
                }
            }

            // 7. Bottom Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pagination Placeholder
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PosLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = null, tint = Color.Gray)
                    }
                    Text(" 1 / 1 ", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(PosLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    
                    // Create Delivery Toggle
                    if (selectedCustomer != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            androidx.compose.material3.Checkbox(
                                checked = createDelivery,
                                onCheckedChange = { createDelivery = it },
                                colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = PosGreen)
                            )
                            Text("Delivery", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Button(
                        onClick = { viewModel.clearCart() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PosBackground,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Bill", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.checkout("CASH", selectedCustomer?.id, createDelivery) },
                        enabled = uiState.cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PosGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductPosCard(product: Product, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .width(100.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder for Product Image
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PosBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.LightGray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "SKU: ${product.id.take(6)}",
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    onQuantityClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PosBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.ShoppingCart, contentDescription = null, tint = Color.LightGray)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Name and Price
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product.name,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Text(
                    text = "${item.product.selling_price} DA",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Delete
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PosRed)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                
                // Minus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PosLightGreen)
                        .clickable { onDecrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", color = PosGreen, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                
                // Quantity
                Text(
                    text = "${item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clickable { onQuantityClick() }
                        .padding(4.dp) // extra padding for larger touch target
                )
                
                // Plus
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PosGreen)
                        .clickable { onIncrease() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
