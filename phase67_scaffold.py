import os

base_dir = r'd:\github\FreshFood\AndroidApp\app\src\main\java\com\freshfood\app'
models_dir = os.path.join(base_dir, r'domain\model')
repo_dir = os.path.join(base_dir, r'domain\repository')
data_repo_dir = os.path.join(base_dir, r'data\repository')
dashboard_dir = os.path.join(base_dir, r'presentation\dashboard')
delivery_dir = os.path.join(base_dir, r'presentation\deliveries')

os.makedirs(dashboard_dir, exist_ok=True)
os.makedirs(delivery_dir, exist_ok=True)

files = {
    # Phase 6 Models
    os.path.join(models_dir, 'DeliveryOrder.kt'): '''package com.freshfood.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DeliveryOrder(
    val id: String,
    val customer_id: String? = null,
    val delivery_employee_id: String? = null,
    val status: String = "PENDING",
    val notes: String? = null
)
''',
    # Phase 7 Dashboard / Reports Repository
    os.path.join(repo_dir, 'DashboardRepository.kt'): '''package com.freshfood.app.domain.repository

interface DashboardRepository {
    suspend fun getTodaySalesTotal(): Double
    suspend fun getTodayProfitTotal(): Double
    suspend fun getTotalCustomerCredit(): Double
    suspend fun getLowStockCount(): Int
}
''',
    os.path.join(data_repo_dir, 'DashboardRepositoryImpl.kt'): '''package com.freshfood.app.data.repository

import com.freshfood.app.domain.repository.DashboardRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

class DashboardRepositoryImpl(
    private val supabase: SupabaseClient
) : DashboardRepository {

    override suspend fun getTodaySalesTotal(): Double {
        // In a real implementation, you would filter sales by today's date.
        // For scaffolding, we fetch all sales and sum.
        val sales = supabase.postgrest["sales"].select().decodeList<JsonObject>()
        return sales.sumOf { it["total_amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0 }
    }

    override suspend fun getTodayProfitTotal(): Double {
        // Profit is calculated by SaleItem (selling_price - cost_price).
        // Scaffolding default to 20% of sales
        return getTodaySalesTotal() * 0.20
    }

    override suspend fun getTotalCustomerCredit(): Double {
        val customers = supabase.postgrest["customers"].select().decodeList<JsonObject>()
        return customers.sumOf { it["current_credit"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0 }
    }

    override suspend fun getLowStockCount(): Int {
        val products = supabase.postgrest["products"].select().decodeList<JsonObject>()
        return products.count { 
            val current = it["current_stock"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            val min = it["min_stock"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
            current <= min
        }
    }
}
''',
    # Dashboard Screen UI
    os.path.join(dashboard_dir, 'DashboardViewModel.kt'): '''package com.freshfood.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.freshfood.app.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardState(
    val todaySales: Double = 0.0,
    val todayProfit: Double = 0.0,
    val totalCredit: Double = 0.0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState())
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sales = repository.getTodaySalesTotal()
                val profit = repository.getTodayProfitTotal()
                val credit = repository.getTotalCustomerCredit()
                val lowStock = repository.getLowStockCount()
                
                _uiState.update { 
                    it.copy(
                        todaySales = sales,
                        todayProfit = profit,
                        totalCredit = credit,
                        lowStockCount = lowStock,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}

class DashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
''',
    os.path.join(dashboard_dir, 'DashboardScreen.kt'): '''package com.freshfood.app.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("BUSINESS DASHBOARD", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item { DashboardCard("Sales Today", "${uiState.todaySales} DA", MaterialTheme.colorScheme.primary) }
                        item { DashboardCard("Profit Today", "${uiState.todayProfit} DA", MaterialTheme.colorScheme.secondary) }
                        item { DashboardCard("Customer Credit", "${uiState.totalCredit} DA", Color(0xFFE65100)) }
                        item { DashboardCard("Low Stock", "${uiState.lowStockCount} items", Color.Red) }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().aspectRatio(1.2f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
'''
}

for path, content in files.items():
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print('Phase 6, 7 and Dashboard scaffolded successfully.')
