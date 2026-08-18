package com.devsoft.freshfood.presentation.purchases

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.components.ProductImageView
import com.devsoft.freshfood.presentation.products.ProductsViewModel
import com.devsoft.freshfood.utils.ProductCategoryEmojiResolver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchaseEntryScreen(
    viewModel: PurchaseViewModel,
    productsViewModel: ProductsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val productsState by productsViewModel.uiState.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Purchase / Stock") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Item")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            OutlinedTextField(
                value = uiState.invoiceNumber,
                onValueChange = { viewModel.updateInvoiceNumber(it) },
                label = { Text("Invoice Number (Optional)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Purchase Items", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.items.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No items added yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.items) { item ->
                        val product = productsState.products.find { it.id == item.product_id }
                        val productName = product?.name ?: item.product_id
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductImageView(
                                    imageUrl = product?.image_url,
                                    emoji = product?.emoji,
                                    productName = productName,
                                    size = 48.dp,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(productName, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Qty: ${item.quantity}")
                                        Text("Price: ${item.purchase_price} DA")
                                        Text("Exp: ${item.expiration_date}")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            val total = uiState.items.sumOf { it.quantity * it.purchase_price }
            Text("Total: $total DA", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { viewModel.submitPurchase() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.items.isNotEmpty() && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Validate Purchase")
                }
            }
            
            uiState.message?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(msg, color = if (msg.contains("Success") || msg.contains("recorded")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            productsViewModel = productsViewModel,
            products = productsState.products,
            onDismiss = { showAddItemDialog = false },
            onAdd = { product, qty, pPrice, sPrice, expDate ->
                val updatedProduct = product.copy(
                    purchase_price = pPrice,
                    selling_price = sPrice
                )
                productsViewModel.updateProduct(updatedProduct)
                viewModel.addItem(updatedProduct, qty, pPrice, expDate)
                showAddItemDialog = false
            },
            onAddNewProduct = { name, barcode, imageUrl, imageSource, emoji ->
                val newProduct = Product(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
                    barcode = barcode?.takeIf { it.isNotBlank() },
                    image_url = imageUrl,
                    image_source = imageSource,
                    emoji = emoji,
                    purchase_price = 0.0,
                    selling_price = 0.0
                )
                productsViewModel.addProduct(newProduct)
                newProduct
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(
    productsViewModel: ProductsViewModel,
    products: List<Product>,
    onDismiss: () -> Unit,
    onAdd: (Product, Int, Double, Double, String) -> Unit,
    onAddNewProduct: (String, String?, String?, String?, String?) -> Product
) {
    val coroutineScope = rememberCoroutineScope()

    var barcode by remember { mutableStateOf("") }
    var productName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var isSearchingBarcode by remember { mutableStateOf(false) }
    var isNewProductByBarcode by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // Emoji state
    var autoEmoji by remember { mutableStateOf("📦") }

    var quantity by remember { mutableStateOf("10") }
    var purchasePrice by remember { mutableStateOf("0") }
    var sellingPrice by remember { mutableStateOf("0") }
    var marginPercentage by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("2026-12-31") }

    // Automatic Emoji Search
    LaunchedEffect(productName, barcode, selectedProduct) {
        if (selectedProduct != null) {
            autoEmoji = selectedProduct?.emoji?.takeIf { it.isNotBlank() }
                ?: ProductCategoryEmojiResolver.resolveEmoji(selectedProduct?.name)
            return@LaunchedEffect
        }

        val targetName = productName.trim()
        val targetBarcode = barcode.trim().takeIf { it.isNotBlank() }

        if (targetName.isBlank() && targetBarcode.isNullOrBlank()) {
            autoEmoji = "📦"
            return@LaunchedEffect
        }

        // 1. Immediately resolve category emoji for instant UI responsiveness
        autoEmoji = ProductCategoryEmojiResolver.resolveEmoji(targetName)
    }

    fun lookupBarcode(code: String) {
        val clean = code.trim().replace("\n", "").replace("\r", "")
        if (clean.isBlank()) return
        barcode = clean
        isSearchingBarcode = true
        coroutineScope.launch {
            val found = productsViewModel.searchProductByBarcode(clean)
            isSearchingBarcode = false
            if (found != null) {
                selectedProduct = found
                productName = found.name
                barcode = found.barcode ?: clean
                purchasePrice = found.purchase_price.toString()
                sellingPrice = found.selling_price.toString()
                autoEmoji = found.emoji ?: ProductCategoryEmojiResolver.resolveEmoji(found.name)
                isNewProductByBarcode = false
                expanded = false
            } else {
                selectedProduct = null
                isNewProductByBarcode = true
            }
        }
    }

    val filteredProducts = if (productName.isBlank()) {
        products
    } else {
        products.filter {
            it.name.contains(productName, ignoreCase = true) || 
            it.barcode?.contains(productName, ignoreCase = true) == true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Purchase") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // 1. Barcode Field (supports manual typing and keyboard scanner input)
                OutlinedTextField(
                    value = barcode,
                    onValueChange = { input ->
                        if (input.contains("\n") || input.contains("\r")) {
                            lookupBarcode(input)
                        } else {
                            barcode = input
                            if (input.isBlank()) {
                                isNewProductByBarcode = false
                            }
                        }
                    },
                    label = { Text("Barcode") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = { lookupBarcode(barcode) },
                        onDone = { lookupBarcode(barcode) }
                    ),
                    trailingIcon = {
                        if (isSearchingBarcode) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { lookupBarcode(barcode) }) {
                                Icon(Icons.Filled.Search, contentDescription = "Scan / Search Barcode")
                            }
                        }
                    }
                )

                if (isNewProductByBarcode) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "New barcode detected. Enter product name below to register.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (selectedProduct != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Existing product loaded: ${selectedProduct?.name}",
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. Product Name / Search Field
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = productName,
                        onValueChange = { 
                            productName = it
                            expanded = it.isNotBlank()
                            if (selectedProduct != null && selectedProduct?.name != it) {
                                selectedProduct = null
                            }
                        },
                        label = { Text("Product Name / Search") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded && filteredProducts.isNotEmpty(),
                        onDismissRequest = { expanded = false },
                        properties = PopupProperties(focusable = false)
                    ) {
                        filteredProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        ProductImageView(
                                            imageUrl = product.image_url,
                                            emoji = product.emoji,
                                            productName = product.name,
                                            size = 36.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(product.name, fontWeight = FontWeight.Bold)
                                            product.barcode?.let { bc ->
                                                Text("Barcode: $bc", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    selectedProduct = product
                                    productName = product.name
                                    if (!product.barcode.isNullOrBlank()) {
                                        barcode = product.barcode
                                    }
                                    purchasePrice = product.purchase_price.toString()
                                    sellingPrice = product.selling_price.toString()
                                    autoEmoji = product.emoji ?: ProductCategoryEmojiResolver.resolveEmoji(product.name)
                                    isNewProductByBarcode = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // 3. Automatic Product Image / Emoji Preview Card
                if (productName.isNotBlank() || selectedProduct != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProductImageView(
                                imageUrl = null,
                                emoji = autoEmoji,
                                productName = productName.ifBlank { selectedProduct?.name },
                                size = 56.dp,
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Visual: $autoEmoji (Food Emoji)",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { newValue -> 
                        purchasePrice = newValue 
                        val p = newValue.toDoubleOrNull() ?: 0.0
                        val m = marginPercentage.toDoubleOrNull()
                        if (m != null) {
                            val calculated = p * (1 + m / 100)
                            sellingPrice = (Math.round(calculated * 10.0) / 10.0).toString()
                        }
                    },
                    label = { Text("Purchase Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = marginPercentage,
                    onValueChange = { newValue -> 
                        marginPercentage = newValue
                        val p = purchasePrice.toDoubleOrNull() ?: 0.0
                        val m = newValue.toDoubleOrNull()
                        if (m != null) {
                            val calculated = p * (1 + m / 100)
                            sellingPrice = (Math.round(calculated * 10.0) / 10.0).toString()
                        }
                    },
                    label = { Text("Margin Percentage (%)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it },
                    label = { Text("Expiration Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    val pPrice = purchasePrice.toDoubleOrNull() ?: 0.0
                    val sPrice = sellingPrice.toDoubleOrNull() ?: 0.0
                    if (productName.isBlank() || qty <= 0) return@Button

                    val currentBarcode = barcode.trim().takeIf { it.isNotBlank() }
                    val finalEmoji = autoEmoji

                    if (selectedProduct != null) {
                        var targetProduct = selectedProduct!!
                        if (targetProduct.barcode != currentBarcode && currentBarcode != null) {
                            targetProduct = targetProduct.copy(barcode = currentBarcode)
                        }
                        if (targetProduct.image_url == null) {
                            targetProduct = targetProduct.copy(image_url = null, image_source = "emoji", emoji = finalEmoji)
                        }
                        onAdd(targetProduct, qty, pPrice, sPrice, expirationDate)
                    } else {
                        val newProduct = onAddNewProduct(
                            productName.trim(), 
                            currentBarcode, 
                            null, 
                            "emoji", 
                            finalEmoji
                        )
                        onAdd(newProduct, qty, pPrice, sPrice, expirationDate)
                    }
                },
                enabled = productName.isNotBlank() && (quantity.toIntOrNull() ?: 0) > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
