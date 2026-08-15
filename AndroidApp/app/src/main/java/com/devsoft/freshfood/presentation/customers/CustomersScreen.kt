package com.devsoft.freshfood.presentation.customers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.devsoft.freshfood.domain.model.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
    onOpenDrawer: () -> Unit = {},
    onAddCustomerClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCustomerForPayment by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers & Credit", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(
                onClick = onAddCustomerClick,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Customer")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search customers...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (uiState) {
                is CustomersUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CustomersUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${(uiState as CustomersUiState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is CustomersUiState.Success -> {
                    val customers = (uiState as CustomersUiState.Success).customers
                    val filteredCustomers = customers.filter { it.name.contains(searchQuery, ignoreCase = true) || (it.phone?.contains(searchQuery) == true) }

                    if (filteredCustomers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No customers found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp) // space for FAB
                        ) {
                            items(filteredCustomers) { customer ->
                                CustomerCard(
                                    customer = customer,
                                    onClick = { selectedCustomerForPayment = customer }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedCustomerForPayment?.let { customer ->
        PaymentDialog(
            customer = customer,
            onDismiss = { selectedCustomerForPayment = null },
            onSubmit = { amount ->
                viewModel.registerPayment(customer.id, amount)
                selectedCustomerForPayment = null
            }
        )
    }
}

@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = customer.phone ?: "No Phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Credit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "${customer.current_credit} DA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (customer.current_credit > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PaymentDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSubmit: (Double) -> Unit
) {
    var paymentAmount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Payment") },
        text = {
            Column {
                Text("Customer: ${customer.name}", fontWeight = FontWeight.Bold)
                Text("Current Debt: ${customer.current_credit} DA", color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = paymentAmount,
                    onValueChange = { paymentAmount = it },
                    label = { Text("Amount Paid (DA)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amount = paymentAmount.toDoubleOrNull()
                if (amount != null && amount > 0) {
                    onSubmit(amount)
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
