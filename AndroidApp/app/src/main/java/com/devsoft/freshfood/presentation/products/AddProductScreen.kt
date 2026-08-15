package com.devsoft.freshfood.presentation.products

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
    viewModel: com.devsoft.freshfood.presentation.products.ProductsViewModel,
    initialBarcode: String? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf(initialBarcode ?: "") }
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
                actions = {
                    com.devsoft.freshfood.presentation.components.GlobalSyncButton()
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
                onClick = { 
                    if (name.isNotBlank()) {
                        val product = com.devsoft.freshfood.domain.model.Product(
                            id = java.util.UUID.randomUUID().toString(),
                            name = name,
                            barcode = sku.ifBlank { null },
                            selling_price = sellPrice.toDoubleOrNull() ?: 0.0,
                            purchase_price = costPrice.toDoubleOrNull() ?: 0.0,
                            current_stock = currentStock.toIntOrNull() ?: 0
                        )
                        viewModel.addProduct(product)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Save Product")
            }
        }
    }
}
