package com.devsoft.freshfood.presentation.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.components.BarcodeScannerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductsViewModel,
    onAddProductClick: (String?) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    var showScanner by remember { mutableStateOf(false) }
    var selectedProductForStock by remember { mutableStateOf<Product?>(null) }
    var stockQuantity by remember { mutableStateOf("") }
    var stockExpiration by remember { mutableStateOf("") }
    
    var selectedProductForEdit by remember { mutableStateOf<Product?>(null) }
    var editName by remember { mutableStateOf("") }
    var editBarcode by remember { mutableStateOf("") }
    var editPurchasePrice by remember { mutableStateOf("") }
    var editSellingPrice by remember { mutableStateOf("") }
    var editStock by remember { mutableStateOf("") }
    var lotCount by remember { mutableStateOf(0) }

    LaunchedEffect(selectedProductForEdit) {
        selectedProductForEdit?.let { product ->
            editName = product.name
            editBarcode = product.barcode ?: ""
            editPurchasePrice = product.purchase_price.toString()
            editSellingPrice = product.selling_price.toString()
            editStock = product.current_stock.toString()
            lotCount = viewModel.getLotCount(product.id)
        }
    }

    if (showScanner) {
        BarcodeScannerScreen(onBarcodeScanned = { barcode ->
            showScanner = false
            val product = viewModel.findProductByBarcode(barcode)
            if (product != null) {
                selectedProductForStock = product
            } else {
                onAddProductClick(barcode)
            }
        })
        return
    }

    if (selectedProductForStock != null) {
        AlertDialog(
            onDismissRequest = { selectedProductForStock = null },
            title = { Text("Add Stock: ${selectedProductForStock?.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Quantity") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stockExpiration,
                        onValueChange = { stockExpiration = it },
                        label = { Text("Expiration Date (YYYY-MM-DD)") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val qty = stockQuantity.toIntOrNull() ?: 0
                    if (qty > 0) {
                        viewModel.addStock(selectedProductForStock!!, qty, stockExpiration)
                    }
                    selectedProductForStock = null
                    stockQuantity = ""
                    stockExpiration = ""
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForStock = null }) { Text("Cancel") }
            }
        )
    }

    if (selectedProductForEdit != null) {
        AlertDialog(
            onDismissRequest = { selectedProductForEdit = null },
            title = { Text("Edit Product") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBarcode,
                        onValueChange = { editBarcode = it },
                        label = { Text("Barcode") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPurchasePrice,
                        onValueChange = { editPurchasePrice = it },
                        label = { Text("Purchase Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editSellingPrice,
                        onValueChange = { editSellingPrice = it },
                        label = { Text("Selling Price") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it },
                        label = { Text("Stock") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Total Lots: $lotCount",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val updatedProduct = selectedProductForEdit!!.copy(
                        name = editName,
                        barcode = editBarcode.takeIf { it.isNotBlank() },
                        purchase_price = editPurchasePrice.toDoubleOrNull() ?: selectedProductForEdit!!.purchase_price,
                        selling_price = editSellingPrice.toDoubleOrNull() ?: selectedProductForEdit!!.selling_price,
                        current_stock = editStock.toIntOrNull() ?: selectedProductForEdit!!.current_stock
                    )
                    viewModel.updateProduct(updatedProduct)
                    selectedProductForEdit = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForEdit = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products & Stock", fontWeight = FontWeight.Bold) },
                navigationIcon = {
IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
            FloatingActionButton(onClick = { onAddProductClick(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Product")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search by name or barcode...") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = {
                    IconButton(onClick = { showScanner = true }) {
                        Text("📷") // Simple camera/scan indicator
                    }
                },
                singleLine = true
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (uiState.error != null) {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    if (uiState.filteredProducts.isEmpty()) {
                        Text("No products found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.filteredProducts) { product ->
                                ProductCard(
                                    product = product,
                                    onClick = { selectedProductForStock = product },
                                    onEditClick = { selectedProductForEdit = product }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(product: Product, onClick: () -> Unit, onEditClick: () -> Unit) {
    val isLowStock = product.current_stock <= product.min_stock
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Price: ${product.selling_price} DA", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Stock: ${product.current_stock} ${product.unit}", style = MaterialTheme.typography.bodyMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isLowStock) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Low Stock",
                        tint = Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(imageVector = Icons.Filled.Edit, contentDescription = "Edit Product")
                }
            }
        }
    }
}
