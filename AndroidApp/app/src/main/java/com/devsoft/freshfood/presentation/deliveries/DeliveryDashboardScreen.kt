package com.devsoft.freshfood.presentation.deliveries

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryDashboardScreen(
    viewModel: DeliveryViewModel,
    currentUserId: String?,
    currentUserRole: String,
    onDeliveryClick: (String) -> Unit,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Pending", "Out for Delivery", "Delivered")

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadDeliveries()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Deliveries", fontWeight = FontWeight.Bold) },
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
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            when (val state = uiState) {
                is DeliveryUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DeliveryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
                is DeliveryUiState.Success -> {
                    val filterStatus = when (selectedTabIndex) {
                        0 -> listOf("PENDING", "ASSIGNED")
                        1 -> listOf("OUT_FOR_DELIVERY")
                        2 -> listOf("DELIVERED", "PARTIALLY_DELIVERED")
                        else -> emptyList()
                    }
                    val baseOrders = if (currentUserRole == "DELIVERY" && currentUserId != null) {
                        state.deliveries.filter { it.order.delivery_employee_id == currentUserId }
                    } else {
                        state.deliveries
                    }
                    val filteredOrders = baseOrders.filter { it.order.status in filterStatus }

                    if (filteredOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No deliveries found for this status.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredOrders) { details ->
                                DeliveryOrderCard(
                                    details = details,
                                    onClick = { onDeliveryClick(details.order.id) },
                                    canDelete = currentUserRole == "ADMIN" && details.order.status in listOf("PENDING", "ASSIGNED", "OUT_FOR_DELIVERY"),
                                    onDeleteClick = { viewModel.deleteDeliveryOrder(details.order.id) }
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
fun DeliveryOrderCard(
    details: DeliveryOrderWithDetails,
    onClick: () -> Unit,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Order ID: ${details.order.id.take(8)}...", 
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (canDelete) {
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Order", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Customer: ${details.customer?.name ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${details.items.size} Items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Badge(
                    containerColor = when(details.order.status) {
                        "PENDING" -> MaterialTheme.colorScheme.error
                        "OUT_FOR_DELIVERY" -> MaterialTheme.colorScheme.tertiary
                        "DELIVERED" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.secondary
                    }
                ) {
                    Text(details.order.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
}
