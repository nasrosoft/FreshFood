package com.devsoft.freshfood.presentation.products

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.freshfood.R
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.components.BarcodeScannerScreen
import com.devsoft.freshfood.presentation.components.ProductImageView
import com.devsoft.freshfood.ui.theme.*

enum class ProductFilter(val stringRes: Int) {
    ALL(R.string.all),
    LOW_STOCK(R.string.low_stock),
    EXPIRING(R.string.expiring),
    EXPIRED(R.string.expired)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductsViewModel,
    onAddProductClick: (String?) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }

    var selectedFilter by remember { mutableStateOf(ProductFilter.ALL) }
    var showScanner by remember { mutableStateOf(false) }

    var selectedProductForDetail by remember { mutableStateOf<Product?>(null) }
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
                selectedProductForDetail = product
            } else {
                onAddProductClick(barcode)
            }
        })
        return
    }

    // Filter products based on selected filter tab
    val displayedProducts = remember(uiState.filteredProducts, selectedFilter) {
        when (selectedFilter) {
            ProductFilter.ALL -> uiState.filteredProducts
            ProductFilter.LOW_STOCK -> uiState.filteredProducts.filter { it.current_stock <= it.min_stock }
            ProductFilter.EXPIRING -> uiState.filteredProducts // Placeholder for expiring filter
            ProductFilter.EXPIRED -> uiState.filteredProducts.filter { it.current_stock <= 0 }
        }
    }

    Scaffold(
        topBar = {
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    // Top App Bar
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
                            Text(
                                stringResource(R.string.products),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { onAddProductClick(null) }) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add), tint = PrimaryBlue)
                            }
                        }
                    }

                    // Search Bar with Barcode Scan Button
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text(stringResource(R.string.search_products_placeholder), color = TextMuted, fontSize = 14.sp) },
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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips (Responsive Horizontal Scroll)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProductFilter.values().forEach { filter ->
                            val isSelected = selectedFilter == filter
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryBlue else CardSurfaceVariant)
                                    .clickable { selectedFilter = filter }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    stringResource(filter.stringRes),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextMuted,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            } else if (displayedProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(stringResource(R.string.no_products_found), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(displayedProducts) { product ->
                        ProductGridCard(
                            product = product,
                            onClick = { selectedProductForDetail = product }
                        )
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // PRODUCT DETAIL DIALOG / BOTTOM SHEET
    // -------------------------------------------------------------
    if (selectedProductForDetail != null) {
        val product = selectedProductForDetail!!
        val profitUnit = product.selling_price - product.purchase_price
        val margin = if (product.selling_price > 0) (profitUnit / product.selling_price) * 100 else 0.0

        AlertDialog(
            onDismissRequest = { selectedProductForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.product_details), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedProductForDetail = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Big Product Image Preview
                    ProductImageView(
                        imageUrl = product.image_url,
                        emoji = product.emoji,
                        productName = product.name,
                        size = 110.dp,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(product.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                    product.barcode?.let { bc ->
                        Text("Barcode: $bc", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pricing breakdown card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            DetailRow(stringResource(R.string.selling_price_label), "${product.selling_price} DA", FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            DetailRow(stringResource(R.string.purchase_price_label), "${product.purchase_price} DA", FontWeight.Normal)
                            Spacer(modifier = Modifier.height(4.dp))
                            DetailRow("Profit / Unit", "${String.format(java.util.Locale.US, "%.1f", profitUnit)} DA", FontWeight.SemiBold, StatusSuccess)
                            Spacer(modifier = Modifier.height(4.dp))
                            DetailRow("Margin", "${String.format(java.util.Locale.US, "%.1f", margin)}%", FontWeight.SemiBold, StatusSuccess)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stock card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(R.string.current_stock_label), style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                Text("${product.current_stock} ${product.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    selectedProductForStock = product
                                    selectedProductForDetail = null
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(stringResource(R.string.add_stock))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    selectedProductForEdit = product
                    selectedProductForDetail = null
                }) {
                    Text(stringResource(R.string.edit_product))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForDetail = null }) { 
                    Text(stringResource(R.string.close)) 
                }
            }
        )
    }

    // -------------------------------------------------------------
    // ADD STOCK DIALOG
    // -------------------------------------------------------------
    if (selectedProductForStock != null) {
        AlertDialog(
            onDismissRequest = { selectedProductForStock = null },
            title = { Text("${stringResource(R.string.add_stock)}: ${selectedProductForStock?.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = stockQuantity,
                        onValueChange = { stockQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = stockExpiration,
                        onValueChange = { stockExpiration = it },
                        label = { Text("Expiration Date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val qty = stockQuantity.toIntOrNull() ?: 0
                    if (qty > 0) {
                        viewModel.addStock(selectedProductForStock!!, qty, stockExpiration)
                    }
                    selectedProductForStock = null
                    stockQuantity = ""
                    stockExpiration = ""
                }) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForStock = null }) { 
                    Text(stringResource(R.string.cancel)) 
                }
            }
        )
    }

    // -------------------------------------------------------------
    // EDIT PRODUCT DIALOG
    // -------------------------------------------------------------
    if (selectedProductForEdit != null) {
        AlertDialog(
            onDismissRequest = { selectedProductForEdit = null },
            title = { Text(stringResource(R.string.edit_product)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editBarcode,
                        onValueChange = { editBarcode = it },
                        label = { Text("Barcode") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPurchasePrice,
                        onValueChange = { editPurchasePrice = it },
                        label = { Text(stringResource(R.string.purchase_price_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editSellingPrice,
                        onValueChange = { editSellingPrice = it },
                        label = { Text(stringResource(R.string.selling_price_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editStock,
                        onValueChange = { editStock = it },
                        label = { Text(stringResource(R.string.current_stock_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
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
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedProductForEdit = null }) { 
                    Text(stringResource(R.string.cancel)) 
                }
            }
        )
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    onClick: () -> Unit
) {
    val isLowStock = product.current_stock <= product.min_stock

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Centered Product Image/Emoji
            ProductImageView(
                imageUrl = product.image_url,
                emoji = product.emoji,
                productName = product.name,
                size = 72.dp,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Product Name (Responsive 2-line layout)
            Text(
                text = product.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 17.sp),
                fontWeight = FontWeight.Bold,
                color = TextDark,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Selling Price
            Text(
                text = "${product.selling_price} DA",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Stock Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isLowStock) StatusWarningContainer else StatusSuccessContainer)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = stringResource(R.string.stock_label, product.current_stock),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isLowStock) StatusWarning else StatusSuccess
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, weight: FontWeight = FontWeight.Normal, color: Color = TextDark) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = weight, color = color)
    }
}
