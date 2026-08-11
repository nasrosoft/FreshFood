import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\freshfood\app'
models_dir = os.path.join(base_dir, r'domain\model')
repo_dir = os.path.join(base_dir, r'domain\repository')
data_repo_dir = os.path.join(base_dir, r'data\repository')
customers_presentation_dir = os.path.join(base_dir, r'presentation\customers')

os.makedirs(customers_presentation_dir, exist_ok=True)

files = {
    os.path.join(models_dir, 'Customer.kt'): '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Customer(
    val id: String,
    val name: String,
    val phone: String? = null,
    val address: String? = null,
    val wilaya: String? = null,
    val commune: String? = null,
    val photo_url: String? = null,
    val credit_limit: Double = 0.0,
    val current_credit: Double = 0.0,
    val customer_type: String? = null,
    val is_active: Boolean = true,
    val notes: String? = null
)
''',
    os.path.join(models_dir, 'Payment.kt'): '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Payment(
    val id: String? = null,
    val customer_id: String,
    val amount: Double,
    val payment_method: String,
    val reference_id: String? = null,
    val user_id: String,
    val notes: String? = null
)
''',
    os.path.join(repo_dir, 'CustomerRepository.kt'): '''package com.freshfood.app.domain.repository

import com.freshfood.app.domain.model.Customer
import com.freshfood.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getCustomers(): Flow<List<Customer>>
    suspend fun getCustomerById(id: String): Customer?
    suspend fun registerPayment(payment: Payment)
}
''',
    os.path.join(data_repo_dir, 'CustomerRepositoryImpl.kt'): '''package com.freshfood.app.data.repository

import com.freshfood.app.domain.model.Customer
import com.freshfood.app.domain.model.Payment
import com.freshfood.app.domain.repository.CustomerRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CustomerRepositoryImpl(
    private val supabase: SupabaseClient
) : CustomerRepository {

    override suspend fun getCustomers(): Flow<List<Customer>> = flow {
        val customers = supabase.postgrest["customers"]
            .select()
            .decodeList<Customer>()
        emit(customers)
    }

    override suspend fun getCustomerById(id: String): Customer? {
        return supabase.postgrest["customers"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Customer>()
    }

    override suspend fun registerPayment(payment: Payment) {
        // We use an RPC function or a single transaction. 
        // Here we insert the payment directly. The trigger `trigger_update_customer_credit` 
        // on `credit_transactions` will handle updating `current_credit`.
        // Wait, to trigger that we need to insert a credit_transaction.
        // For now, we insert payment and credit_transaction manually or let a Postgres function handle it.
        // Inserting Payment:
        supabase.postgrest["payments"].insert(payment)
        
        // Inserting Credit Transaction (PAYMENT)
        val creditTx = mapOf(
            "customer_id" to payment.customer_id,
            "amount" to payment.amount,
            "transaction_type" to "PAYMENT",
            "user_id" to payment.user_id
        )
        supabase.postgrest["credit_transactions"].insert(creditTx)
    }
}
''',
    os.path.join(customers_presentation_dir, 'CustomersViewModel.kt'): '''package com.freshfood.app.presentation.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freshfood.app.domain.model.Customer
import com.freshfood.app.domain.repository.CustomerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class CustomersUiState {
    object Loading : CustomersUiState()
    data class Success(val customers: List<Customer>) : CustomersUiState()
    data class Error(val message: String) : CustomersUiState()
}

class CustomersViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CustomersUiState>(CustomersUiState.Loading)
    val uiState: StateFlow<CustomersUiState> = _uiState.asStateFlow()

    init {
        loadCustomers()
    }

    fun loadCustomers() {
        viewModelScope.launch {
            _uiState.value = CustomersUiState.Loading
            repository.getCustomers()
                .catch { e ->
                    _uiState.value = CustomersUiState.Error(e.message ?: "Unknown Error")
                }
                .collect { customers ->
                    _uiState.value = CustomersUiState.Success(customers)
                }
        }
    }
}

class CustomersViewModelFactory(
    private val repository: CustomerRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
''',
    os.path.join(customers_presentation_dir, 'CustomersScreen.kt'): '''package com.freshfood.app.presentation.customers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freshfood.app.domain.model.Customer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(
    viewModel: CustomersViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers & Credit") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is CustomersUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is CustomersUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as CustomersUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is CustomersUiState.Success -> {
                    val customers = (uiState as CustomersUiState.Success).customers
                    if (customers.isEmpty()) {
                        Text("No customers found.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(customers) { customer ->
                                CustomerCard(customer = customer)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerCard(customer: Customer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = customer.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = customer.phone ?: "No Phone", style = MaterialTheme.typography.bodyMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Credit:", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${customer.current_credit} DA",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (customer.current_credit > 0) Color.Red else Color.Green
                )
            }
        }
    }
}
'''
}

for path, content in files.items():
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print('Phase 5 (Customers & Credit) scaffolded successfully.')
