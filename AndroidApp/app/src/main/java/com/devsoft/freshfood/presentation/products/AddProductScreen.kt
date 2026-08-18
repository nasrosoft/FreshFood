package com.devsoft.freshfood.presentation.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.presentation.components.ProductImageView
import com.devsoft.freshfood.utils.ProductCategoryEmojiResolver
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductsViewModel,
    initialBarcode: String? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var sku by remember { mutableStateOf(initialBarcode ?: "") }
    var sellPrice by remember { mutableStateOf("") }
    var costPrice by remember { mutableStateOf("") }
    var currentStock by remember { mutableStateOf("") }

    // Emoji state
    var autoEmoji by remember { mutableStateOf("📦") }

    LaunchedEffect(name, sku) {
        val targetName = name.trim()
        val targetSku = sku.trim().takeIf { it.isNotBlank() }

        if (targetName.isBlank() && targetSku.isNullOrBlank()) {
            autoEmoji = "📦"
            return@LaunchedEffect
        }

        autoEmoji = ProductCategoryEmojiResolver.resolveEmoji(targetName)
    }

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
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Product Name") }, 
                modifier = Modifier.fillMaxWidth()
            )

            // Visual Preview Card
            if (name.isNotBlank()) {
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
                            productName = name,
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

            OutlinedTextField(
                value = sku, 
                onValueChange = { sku = it }, 
                label = { Text("Barcode (SKU)") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            OutlinedTextField(
                value = sellPrice, 
                onValueChange = { sellPrice = it }, 
                label = { Text("Selling Price (DA)") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = costPrice, 
                onValueChange = { costPrice = it }, 
                label = { Text("Cost Price (DA)") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = currentStock, 
                onValueChange = { currentStock = it }, 
                label = { Text("Initial Stock Quantity") }, 
                modifier = Modifier.fillMaxWidth(), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { 
                    if (name.isNotBlank()) {
                        val finalEmoji = autoEmoji

                        val product = Product(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            barcode = sku.trim().ifBlank { null },
                            image_url = null,
                            image_source = "emoji",
                            emoji = finalEmoji,
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
