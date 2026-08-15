package com.devsoft.freshfood.presentation.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.ReturnOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnsScreen(
    viewModel: ReturnsViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customer Returns", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = { /* TODO: Open New Return Dialog */ }) {
                Icon(Icons.Filled.Add, contentDescription = "New Return")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ReturnsUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ReturnsUiState.Error -> {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                }
                is ReturnsUiState.Success -> {
                    if (state.returns.isEmpty()) {
                        Text("No returns found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.returns) { ret ->
                                ReturnOrderCard(ret, onApprove = { viewModel.updateReturnStatus(ret.id, "APPROVED") })
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
fun ReturnOrderCard(returnOrder: ReturnOrder, onApprove: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Return ID: ${returnOrder.id.take(8)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Qty: ${returnOrder.quantity}")
            Text("Reason: ${returnOrder.reason ?: "Unknown"}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Badge(containerColor = if (returnOrder.status == "PENDING") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) {
                    Text(returnOrder.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                
                if (returnOrder.status == "PENDING") {
                    TextButton(onClick = onApprove) {
                        Text("Approve")
                    }
                }
            }
        }
    }
}
