import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\freshfood\app'
models_dir = os.path.join(base_dir, r'domain\model')
repo_dir = os.path.join(base_dir, r'domain\repository')
data_repo_dir = os.path.join(base_dir, r'data\repository')
sales_presentation_dir = os.path.join(base_dir, r'presentation\sales')

os.makedirs(models_dir, exist_ok=True)
os.makedirs(repo_dir, exist_ok=True)
os.makedirs(data_repo_dir, exist_ok=True)
os.makedirs(sales_presentation_dir, exist_ok=True)

files = {
    os.path.join(models_dir, 'Sale.kt'): '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sale(
    val id: String? = null,
    val invoice_number: String? = null,
    val customer_id: String? = null,
    val user_id: String? = null,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String,
    val status: String = "COMPLETED"
)
''',
    os.path.join(models_dir, 'SaleRequest.kt'): '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SaleRequest(
    val customer_id: String? = null,
    val user_id: String,
    val total_amount: Double,
    val paid_amount: Double,
    val credit_amount: Double,
    val payment_method: String,
    val items: List<SaleItemRequest>
)

@Serializable
data class SaleItemRequest(
    val product_id: String,
    val quantity: Int,
    val unit_price: Double
)
''',
    os.path.join(repo_dir, 'SalesRepository.kt'): '''package com.freshfood.app.domain.repository

import com.freshfood.app.domain.model.SaleRequest

interface SalesRepository {
    suspend fun processSale(saleRequest: SaleRequest): Result<String>
}
''',
    os.path.join(data_repo_dir, 'SalesRepositoryImpl.kt'): '''package com.freshfood.app.data.repository

import com.freshfood.app.domain.model.SaleRequest
import com.freshfood.app.domain.repository.SalesRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class SalesRepositoryImpl(
    private val supabase: SupabaseClient
) : SalesRepository {

    override suspend fun processSale(saleRequest: SaleRequest): Result<String> {
        return try {
            // Invoking the PostgreSQL RPC function 'process_sale'
            // The argument must match the parameter name defined in the SQL function: 'sale_data'
            val response = supabase.postgrest.rpc(
                function = "process_sale",
                parameters = mapOf("sale_data" to saleRequest)
            )
            
            // Expected JSON response: {"success": true, "sale_id": "...", "invoice_number": "..."}
            val jsonResponse = response.decodeAs<JsonObject>()
            val invoiceNumber = jsonResponse["invoice_number"]?.jsonPrimitive?.content ?: "Unknown"
            Result.success(invoiceNumber)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
''',
    os.path.join(sales_presentation_dir, 'PosViewModel.kt'): '''package com.freshfood.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freshfood.app.domain.model.Product
import com.freshfood.app.domain.model.SaleItemRequest
import com.freshfood.app.domain.model.SaleRequest
import com.freshfood.app.domain.repository.SalesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartItem(
    val product: Product,
    var quantity: Int
)

data class PosUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val checkoutMessage: String? = null
)

class PosViewModel(
    private val salesRepository: SalesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PosUiState())
    val uiState: StateFlow<PosUiState> = _uiState.asStateFlow()

    // Temporary mock user_id for Phase 4 (Will be fetched from Auth in production)
    private val MOCK_USER_ID = "00000000-0000-0000-0000-000000000000"

    fun addToCart(product: Product) {
        _uiState.update { state ->
            val existingItem = state.cartItems.find { it.product.id == product.id }
            val newItems = if (existingItem != null) {
                state.cartItems.map { 
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.cartItems + CartItem(product, 1)
            }
            state.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
        }
    }

    fun clearCart() {
        _uiState.update { it.copy(cartItems = emptyList(), totalAmount = 0.0, checkoutMessage = null) }
    }

    private fun calculateTotal(items: List<CartItem>): Double {
        return items.sumOf { it.product.selling_price * it.quantity }
    }

    fun checkout(paymentMethod: String) {
        val currentState = _uiState.value
        if (currentState.cartItems.isEmpty()) return
        
        _uiState.update { it.copy(isLoading = true, checkoutMessage = null) }

        viewModelScope.launch {
            val itemsReq = currentState.cartItems.map {
                SaleItemRequest(
                    product_id = it.product.id,
                    quantity = it.quantity,
                    unit_price = it.product.selling_price
                )
            }
            
            val saleReq = SaleRequest(
                customer_id = null, // Guest checkout for now
                user_id = MOCK_USER_ID, 
                total_amount = currentState.totalAmount,
                paid_amount = currentState.totalAmount, // Assuming full payment in CASH/CARD
                credit_amount = 0.0,
                payment_method = paymentMethod,
                items = itemsReq
            )

            val result = salesRepository.processSale(saleReq)
            
            _uiState.update { state ->
                if (result.isSuccess) {
                    state.copy(
                        isLoading = false,
                        cartItems = emptyList(),
                        totalAmount = 0.0,
                        checkoutMessage = "Success! Invoice: ${result.getOrNull()}"
                    )
                } else {
                    state.copy(
                        isLoading = false,
                        checkoutMessage = "Failed: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }
}

class PosViewModelFactory(
    private val salesRepository: SalesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PosViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PosViewModel(salesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
''',
    os.path.join(sales_presentation_dir, 'PosScreen.kt'): '''package com.freshfood.app.presentation.sales

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(
    viewModel: PosViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Point of Sale (POS)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            // Cart Items
            Text("Cart Items", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.cartItems.isEmpty()) {
                Text("Cart is empty", modifier = Modifier.weight(1f))
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(uiState.cartItems) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.quantity}x ${item.product.name}")
                            Text("${item.quantity * item.product.selling_price} DA")
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total:", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("${uiState.totalAmount} DA", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(
                        onClick = { viewModel.checkout("CASH") },
                        enabled = uiState.cartItems.isNotEmpty()
                    ) {
                        Text("Pay CASH")
                    }
                    Button(
                        onClick = { viewModel.clearCart() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Clear")
                    }
                }
            }
            
            uiState.checkoutMessage?.let { msg ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(msg, color = if (msg.startsWith("Success")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            }
        }
    }
}
'''
}

for path, content in files.items():
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print('Phase 4 (Sales & POS) scaffolded successfully.')
