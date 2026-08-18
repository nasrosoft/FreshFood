package com.devsoft.devsoft.presentation.products

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devsoft.devsoft.domain.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    product: Product?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Product Details") },
                navigationIcon = {
IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    com.devsoft.devsoft.presentation.components.GlobalSyncButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (product == null) {
                Text("Product not found.")
                return@Column
            }

            Text("Category: ${product.category_id ?: "Unknown"}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Barcode: ${product.barcode ?: "N/A"}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Pricing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Purchase Price: ${product.purchase_price} DA")
            Text("Selling Price: ${product.selling_price} DA")
            val profit = product.selling_price - product.purchase_price
            Text("Profit / Unit: $profit DA", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Stock Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Current Stock: ${product.current_stock}", fontWeight = FontWeight.Bold, color = if (product.current_stock <= product.min_stock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
            Text("Minimum Stock: ${product.min_stock}")

            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Batches (FEFO Tracking)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            // In a full implementation, we would query the stock_batches table for this product
            // and list them here with their expiration dates.
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Batch List Placeholder")
                    Text("Fetching batches dynamically requires updating ProductsViewModel to fetch batches.")
                }
            }
        }
    }
}
