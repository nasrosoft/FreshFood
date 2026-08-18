package com.devsoft.devsoft.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devsoft.devsoft.domain.model.Customer
import com.devsoft.devsoft.domain.model.Product
import com.devsoft.devsoft.domain.model.Sale
import com.devsoft.devsoft.domain.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId

import com.devsoft.devsoft.R

enum class DashboardTimeRange(val stringRes: Int) {
    DAY(R.string.day),
    WEEK(R.string.week),
    MONTH(R.string.month),
    ALL(R.string.all_time)
}

enum class DashboardDetailType {
    NONE,
    SALES,
    PROFIT,
    CREDIT,
    LOW_STOCK
}

data class DashboardState(
    val timeRange: DashboardTimeRange = DashboardTimeRange.DAY,
    val allSales: List<Sale> = emptyList(),
    val allCustomers: List<Customer> = emptyList(),
    val allProducts: List<Product> = emptyList(),
    val filteredSales: List<Sale> = emptyList(),
    val salesTotal: Double = 0.0,
    val profitTotal: Double = 0.0,
    val totalCredit: Double = 0.0,
    val lowStockProducts: List<Product> = emptyList(),
    val totalDeliveriesToday: Int = 0,
    val completedDeliveriesToday: Int = 0,
    val pendingDeliveriesToday: Int = 0,
    val selectedDetailType: DashboardDetailType = DashboardDetailType.NONE,
    val isLoading: Boolean = false,
    val error: String? = null
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
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sales = repository.getSales()
                val customers = repository.getCustomers()
                val products = repository.getProducts()
                val deliveries = repository.getDeliveryOrders()
                
                val currentRange = _uiState.value.timeRange
                val filteredSales = filterSalesByRange(sales, currentRange)
                val salesTotal = filteredSales.sumOf { it.total_amount }
                val profitTotal = salesTotal * 0.20 // 20% estimated margin
                val totalCredit = customers.sumOf { it.current_credit }
                val lowStock = products.filter { it.current_stock <= it.min_stock }

                val today = LocalDate.now()
                val todayDeliveries = deliveries.filter { order ->
                    val timestamp = order.updated_at ?: order.created_at
                    if (timestamp != null) {
                        try {
                            val orderDate = try {
                                OffsetDateTime.parse(timestamp).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                            } catch (e: Exception) {
                                Instant.parse(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            orderDate == today
                        } catch (e: Exception) {
                            true
                        }
                    } else {
                        true
                    }
                }

                val totalDeliveries = todayDeliveries.size
                val completedDeliveries = todayDeliveries.count { it.status == "DELIVERED" }
                val pendingDeliveries = todayDeliveries.count { 
                    it.status == "PENDING" || it.status == "ASSIGNED" || it.status == "OUT_FOR_DELIVERY" 
                }

                _uiState.update { 
                    it.copy(
                        allSales = sales,
                        allCustomers = customers,
                        allProducts = products,
                        filteredSales = filteredSales,
                        salesTotal = salesTotal,
                        profitTotal = profitTotal,
                        totalCredit = totalCredit,
                        lowStockProducts = lowStock,
                        totalDeliveriesToday = totalDeliveries,
                        completedDeliveriesToday = completedDeliveries,
                        pendingDeliveriesToday = pendingDeliveries,
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setTimeRange(range: DashboardTimeRange) {
        _uiState.update { state ->
            val filtered = filterSalesByRange(state.allSales, range)
            val salesTotal = filtered.sumOf { it.total_amount }
            val profitTotal = salesTotal * 0.20
            state.copy(
                timeRange = range,
                filteredSales = filtered,
                salesTotal = salesTotal,
                profitTotal = profitTotal
            )
        }
    }

    fun showDetail(type: DashboardDetailType) {
        _uiState.update { it.copy(selectedDetailType = type) }
    }

    fun hideDetail() {
        _uiState.update { it.copy(selectedDetailType = DashboardDetailType.NONE) }
    }

    private fun filterSalesByRange(sales: List<Sale>, range: DashboardTimeRange): List<Sale> {
        val today = LocalDate.now()
        return when (range) {
            DashboardTimeRange.DAY -> {
                sales.filter { parseDate(it.created_at) == today }
            }
            DashboardTimeRange.WEEK -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                sales.filter {
                    val d = parseDate(it.created_at)
                    d != null && !d.isBefore(startOfWeek) && !d.isAfter(today)
                }
            }
            DashboardTimeRange.MONTH -> {
                val startOfMonth = today.withDayOfMonth(1)
                sales.filter {
                    val d = parseDate(it.created_at)
                    d != null && !d.isBefore(startOfMonth) && !d.isAfter(today)
                }
            }
            DashboardTimeRange.ALL -> sales
        }
    }

    private fun parseDate(timestamp: String?): LocalDate? {
        if (timestamp.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(timestamp).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
        } catch (e: Exception) {
            try {
                Instant.parse(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()
            } catch (e2: Exception) {
                try {
                    LocalDate.parse(timestamp.take(10))
                } catch (e3: Exception) {
                    null
                }
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
