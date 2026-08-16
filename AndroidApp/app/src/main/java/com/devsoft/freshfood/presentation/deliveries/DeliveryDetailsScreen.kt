package com.devsoft.freshfood.presentation.deliveries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.DeliveryItemDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailsScreen(
    deliveryId: String,
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showPaymentDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("CASH") }

    val details = (uiState as? DeliveryUiState.Success)?.deliveries?.find { it.order.id == deliveryId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery Details", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        if (details == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uiState is DeliveryUiState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Text("Order not found or still loading.")
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
                // Customer Info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Customer Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Name: ${details.customer?.name ?: "N/A"}")
                        Text("Phone: ${details.customer?.phone ?: "N/A"}")
                        Text("Address: ${details.customer?.address ?: "N/A"}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Status: ${details.order.status}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Items to Deliver", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                // Items List
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(details.items) { itemDetail ->
                        DeliveryItemRow(itemDetail)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons based on status
                when (details.order.status) {
                    "PENDING", "ASSIGNED" -> {
                        Button(
                            onClick = { viewModel.updateDeliveryStatus(details.order.id, "OUT_FOR_DELIVERY") },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Mark as Out for Delivery")
                        }
                    }
                    "OUT_FOR_DELIVERY" -> {
                        Button(
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) {
                            Text("Confirm Delivered")
                        }
                    }
                    "DELIVERED" -> {
                        Button(
                            onClick = { },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = false
                        ) {
                            Text("Delivery Completed")
                        }
                    }
                }
            }
        }
    }

    if (showPaymentDialog && details != null) {
        AlertDialog(
            onDismissRequest = { showPaymentDialog = false },
            title = { Text("Confirm Delivery") },
            text = {
                Column {
                    Text("Select Payment Method:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPaymentMethod == "CASH",
                            onClick = { selectedPaymentMethod = "CASH" }
                        )
                        Text("Espèces")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = selectedPaymentMethod == "CREDIT",
                            onClick = { selectedPaymentMethod = "CREDIT" }
                        )
                        Text("Crédit")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPaymentDialog = false
                    viewModel.updateDeliveryStatus(details.order.id, "DELIVERED")
                    
                    val uri = com.devsoft.freshfood.utils.PdfReceiptGenerator.generateDeliveryReceipt(
                        context,
                        details,
                        selectedPaymentMethod
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
                    Text("Confirm & Print")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPaymentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DeliveryItemRow(itemDetail: DeliveryItemDetail) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(itemDetail.product?.name ?: "Unknown Product", fontWeight = FontWeight.Bold)
                Text("Qty: ${itemDetail.item.quantity}")
            }
        }
    }
}
