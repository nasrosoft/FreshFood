package com.devsoft.devsoft.presentation.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.devsoft.R
import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel,
    onOpenDrawer: () -> Unit = {},
    onAddCustomerClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCustomerForDetail by remember { mutableStateOf<Customer?>(null) }
    var selectedCustomerForPayment by remember { mutableStateOf<Customer?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadCustomers()
    }

    Scaffold(
        topBar = {
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
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
                                stringResource(R.string.customers), 
                                style = MaterialTheme.typography.titleLarge, 
                                fontWeight = FontWeight.Bold, 
                                color = TextDark
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            com.devsoft.devsoft.presentation.components.GlobalSyncButton()
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onAddCustomerClick) {
                                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add), tint = PrimaryBlue)
                            }
                        }
                    }

                    // Search input matching reference UI
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        placeholder = { 
                            Text(stringResource(R.string.search_customers_placeholder), color = TextMuted, fontSize = 14.sp) 
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextMuted) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = CardSurfaceVariant,
                            unfocusedContainerColor = CardSurfaceVariant,
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is CustomersUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                }
                is CustomersUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as CustomersUiState.Error).message,
                            color = StatusError
                        )
                    }
                }
                is CustomersUiState.Success -> {
                    val customers = (uiState as CustomersUiState.Success).customers
                    val filteredCustomers = customers.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || (it.phone?.contains(searchQuery) == true) 
                    }

                    if (filteredCustomers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👥", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    stringResource(R.string.no_customers_found), 
                                    style = MaterialTheme.typography.titleMedium, 
                                    fontWeight = FontWeight.Bold, 
                                    color = TextDark
                                )
                                Text(
                                    stringResource(R.string.tap_to_add_customer), 
                                    style = MaterialTheme.typography.bodySmall, 
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredCustomers) { customer ->
                                CustomerCardModern(
                                    customer = customer,
                                    onClick = { selectedCustomerForDetail = customer }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // CUSTOMER DETAIL DIALOG (matching reference design)
    // -------------------------------------------------------------
    if (selectedCustomerForDetail != null) {
        val customer = selectedCustomerForDetail!!
        AlertDialog(
            onDismissRequest = { selectedCustomerForDetail = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.customer_details), fontWeight = FontWeight.Bold)
                    IconButton(onClick = { selectedCustomerForDetail = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(customer.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextDark)
                    customer.phone?.let { phone ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Filled.Call, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(phone, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                    }
                    customer.address?.let { addr ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(addr, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current Credit Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (customer.current_credit > 0) StatusWarningContainer else StatusSuccessContainer)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                stringResource(R.string.current_credit_label), 
                                style = MaterialTheme.typography.bodySmall, 
                                color = if (customer.current_credit > 0) Color(0xFF92400E) else Color(0xFF065F46)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${String.format(java.util.Locale.US, "%,.0f", customer.current_credit)} DA",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (customer.current_credit > 0) Color(0xFFB45309) else Color(0xFF047857)
                            )
                        }
                    }

                    if (customer.current_credit > 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedCustomerForPayment = customer
                                selectedCustomerForDetail = null
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.receive_payment), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCustomerForDetail = null }) { 
                    Text(stringResource(R.string.close)) 
                }
            }
        )
    }

    // -------------------------------------------------------------
    // RECEIVE PAYMENT DIALOG (CASH ONLY)
    // -------------------------------------------------------------
    if (selectedCustomerForPayment != null) {
        val customer = selectedCustomerForPayment!!
        var paymentAmount by remember { mutableStateOf("") }
        val amount = paymentAmount.toDoubleOrNull() ?: 0.0
        val remaining = (customer.current_credit - amount).coerceAtLeast(0.0)

        AlertDialog(
            onDismissRequest = { selectedCustomerForPayment = null },
            title = { Text(stringResource(R.string.receive_payment), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("${customer.name}", fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.current_credit_label), color = TextMuted)
                        Text("${String.format(java.util.Locale.US, "%,.0f", customer.current_credit)} DA", fontWeight = FontWeight.Bold, color = StatusWarning)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = paymentAmount,
                        onValueChange = { paymentAmount = it },
                        label = { Text(stringResource(R.string.payment_amount_da)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.remaining_credit_label), color = TextMuted)
                        Text("${String.format(java.util.Locale.US, "%,.0f", remaining)} DA", fontWeight = FontWeight.Bold, color = if (remaining > 0) StatusWarning else StatusSuccess)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(stringResource(R.string.payment_method), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = TextDark)
                    Spacer(modifier = Modifier.height(6.dp))

                    // ONLY CASH PAYMENT
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💵", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.cash_payment),
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (amount > 0) {
                            viewModel.registerPayment(customer.id, amount)
                        }
                        selectedCustomerForPayment = null
                    },
                    enabled = amount > 0
                ) {
                    Text(stringResource(R.string.confirm_payment))
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedCustomerForPayment = null }) { 
                    Text(stringResource(R.string.cancel)) 
                }
            }
        )
    }
}

@Composable
fun CustomerCardModern(
    customer: Customer,
    onClick: () -> Unit
) {
    val hasCredit = customer.current_credit > 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏪", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                    customer.phone?.let { phone ->
                        Text(phone, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
            }

            // Credit / Paid Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (hasCredit) StatusWarningContainer else StatusSuccessContainer)
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    if (hasCredit) stringResource(R.string.credit_badge, String.format(java.util.Locale.US, "%,.0f", customer.current_credit)) else stringResource(R.string.paid_badge),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (hasCredit) StatusWarning else StatusSuccess
                )
            }
        }
    }
}
