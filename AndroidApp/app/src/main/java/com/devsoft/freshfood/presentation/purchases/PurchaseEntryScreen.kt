package com.devsoft.freshfood.presentation.purchases

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.products.ProductsViewModel

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
                        // Just display product_id for now, in a real app we'd map it to the name
                        val productName = productsState.products.find { it.id == item.product_id }?.name ?: item.product_id
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                Text(msg, color = if (msg.contains("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            products = productsState.products,
            onDismiss = { showAddItemDialog = false },
            onAdd = { product, qty, price, expDate ->
                viewModel.addItem(product, qty, price, expDate)
                showAddItemDialog = false
            },
            onAddNewProduct = { name ->
                val newProduct = Product(
                    id = java.util.UUID.randomUUID().toString(),
                    name = name,
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
    products: List<Product>,
    onDismiss: () -> Unit,
    onAdd: (Product, Int, Double, String) -> Unit,
    onAddNewProduct: (String) -> Product
) {
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    
    var quantity by remember { mutableStateOf("10") }
    var purchasePrice by remember { mutableStateOf("0") }
    var expirationDate by remember { mutableStateOf("2026-12-31") }

    val filteredProducts = if (searchQuery.isBlank()) {
        products
    } else {
        products.filter {
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.barcode?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Item to Purchase") },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { 
                            searchQuery = it
                            expanded = true
                            selectedProduct = null
                        },
                        label = { Text("Search Product by name or barcode") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredProducts.forEach { product ->
                            DropdownMenuItem(
                                text = { Text(product.name) },
                                onClick = {
                                    selectedProduct = product
                                    searchQuery = product.name
                                    purchasePrice = product.purchase_price.toString()
                                    expanded = false
                                }
                            )
                        }
                        if (searchQuery.isNotBlank() && filteredProducts.none { it.name.equals(searchQuery, ignoreCase = true) }) {
                            DropdownMenuItem(
                                text = { Text("Add '$searchQuery' as new product", color = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    val newProduct = onAddNewProduct(searchQuery)
                                    selectedProduct = newProduct
                                    searchQuery = newProduct.name
                                    purchasePrice = "0.0"
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = purchasePrice,
                    onValueChange = { purchasePrice = it },
                    label = { Text("Purchase Price") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it },
                    label = { Text("Expiration Date (YYYY-MM-DD)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                selectedProduct?.let {
                    onAdd(it, quantity.toIntOrNull() ?: 0, purchasePrice.toDoubleOrNull() ?: 0.0, expirationDate)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
