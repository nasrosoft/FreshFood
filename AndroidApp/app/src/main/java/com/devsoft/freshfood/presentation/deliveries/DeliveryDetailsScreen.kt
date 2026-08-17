package com.devsoft.freshfood.presentation.deliveries

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.freshfood.domain.model.DeliveryItemDetail
import com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails
import com.devsoft.freshfood.presentation.components.ProductImageView
import com.devsoft.freshfood.ui.theme.*
import com.devsoft.freshfood.utils.PdfReceiptGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDetailsScreen(
    deliveryId: String,
    viewModel: DeliveryViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showConfirmDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("CASH") }
    
    val modifiedQuantities = remember { mutableStateMapOf<String, Int>() }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    val details = (uiState as? DeliveryUiState.Success)?.deliveries?.find { it.order.id == deliveryId }
    
    LaunchedEffect(details) {
        if (details != null && modifiedQuantities.isEmpty()) {
            details.items.forEach { itemDetail ->
                modifiedQuantities[itemDetail.item.product_id] = itemDetail.item.quantity
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryBlueDark)
                        }
                        Text(
                            "Delivery #${deliveryId.take(5).uppercase()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                    }
                    com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        if (details == null) {
            Box(modifier = Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                if (uiState is DeliveryUiState.Loading) {
                    CircularProgressIndicator(color = PrimaryBlue)
                } else {
                    Text("Order not found.", color = TextMuted)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Customer Information Card (matching reference design)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                details.customer?.name ?: "Customer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                            StatusBadgeSmall(status = details.order.status)
                        }

                        details.customer?.phone?.let { phone ->
                            Text(phone, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }
                        details.customer?.address?.let { address ->
                            Text(address, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Call & Navigate Quick Actions
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            details.customer?.phone?.let { phone ->
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Call Customer", fontSize = 12.sp)
                                }
                            }

                            details.customer?.address?.let { address ->
                                OutlinedButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(address)}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Navigate", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Products to Deliver", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(8.dp))

                // Delivery Items List with Live Quantity Steppers
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(details.items) { itemDetail ->
                        val productId = itemDetail.item.product_id
                        val currentQty = modifiedQuantities[productId] ?: itemDetail.item.quantity
                        val originalQty = itemDetail.item.quantity
                        val unitPrice = itemDetail.product?.selling_price ?: 0.0

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProductImageView(
                                    imageUrl = itemDetail.product?.image_url,
                                    emoji = itemDetail.product?.emoji,
                                    productName = itemDetail.product?.name ?: productId,
                                    size = 44.dp,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(itemDetail.product?.name ?: productId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$unitPrice DA / unit", fontSize = 11.sp, color = TextMuted)
                                }

                                if (details.order.status == "OUT_FOR_DELIVERY") {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CardSurfaceVariant)
                                            .padding(horizontal = 4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { if (currentQty > 0) modifiedQuantities[productId] = currentQty - 1 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Text("-", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 16.sp)
                                        }
                                        Text("$currentQty", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                        IconButton(
                                            onClick = { if (currentQty < originalQty) modifiedQuantities[productId] = currentQty + 1 },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Filled.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else {
                                    Text("Qty: ${itemDetail.item.quantity}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Financial Summary Card
                val originalTotal = details.items.sumOf { (it.product?.selling_price ?: 0.0) * it.item.quantity }
                val updatedTotal = details.items.sumOf { (modifiedQuantities[it.item.product_id] ?: it.item.quantity) * (it.product?.selling_price ?: 0.0) }
                val diff = updatedTotal - originalTotal

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Updated Total", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", updatedTotal)} DA",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                        }
                        if (diff != 0.0) {
                            Text(
                                "Diff: ${String.format(java.util.Locale.US, "%,.0f", diff)} DA",
                                fontWeight = FontWeight.Bold,
                                color = if (diff < 0) StatusError else StatusSuccess
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons based on status
                when (details.order.status) {
                    "PENDING", "ASSIGNED" -> {
                        Button(
                            onClick = { viewModel.updateDeliveryStatus(details.order.id, "OUT_FOR_DELIVERY") },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Start Delivery (Out for Delivery)")
                        }
                    }
                    "OUT_FOR_DELIVERY" -> {
                        Button(
                            onClick = { showConfirmDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                        ) {
                            Text("Complete & Confirm Delivery")
                        }
                    }
                    "DELIVERED" -> {
                        Button(
                            onClick = {
                                val saleItems = details.items.map {
                                    com.devsoft.freshfood.presentation.sales.CartItem(
                                        product = it.product ?: com.devsoft.freshfood.domain.model.Product(
                                            id = it.item.product_id,
                                            name = "Product",
                                            selling_price = it.product?.selling_price ?: 0.0
                                        ),
                                        quantity = it.item.quantity
                                    )
                                }
                                val uri = PdfReceiptGenerator.generateAndGetUri(
                                    context = context,
                                    cartItems = saleItems,
                                    totalAmount = originalTotal
                                )
                                if (uri != null) {
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share Receipt"))
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Print / Share Receipt")
                        }
                    }
                }
            }
        }
    }

    // Confirmation & Payment Dialog
    if (showConfirmDialog && details != null) {
        val updatedTotal = details.items.sumOf { (modifiedQuantities[it.item.product_id] ?: it.item.quantity) * (it.product?.selling_price ?: 0.0) }

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirm Delivery Completion", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Total Collected: ${String.format(java.util.Locale.US, "%,.0f", updatedTotal)} DA", fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Payment Method:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "CASH" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPaymentMethod == "CASH", onClick = { selectedPaymentMethod = "CASH" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cash (Espèces)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPaymentMethod = "CREDIT" }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPaymentMethod == "CREDIT", onClick = { selectedPaymentMethod = "CREDIT" })
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Credit (Crédit Client)", fontWeight = FontWeight.Bold, color = StatusWarning)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalQuantities = details.items.associate { it.item.id to (modifiedQuantities[it.item.product_id] ?: it.item.quantity) }
                        viewModel.updateDeliveryItemsAndComplete(
                            orderId = details.order.id,
                            modifiedQuantities = finalQuantities
                        )
                        showConfirmDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StatusBadgeSmall(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "DELIVERED" -> Triple(StatusSuccessContainer, StatusSuccess, "Delivered")
        "OUT_FOR_DELIVERY" -> Triple(StatusWarningContainer, StatusWarning, "Out for Delivery")
        "ASSIGNED" -> Triple(PrimaryBlueContainer, PrimaryBlue, "Assigned")
        else -> Triple(CardSurfaceVariant, TextMuted, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}
