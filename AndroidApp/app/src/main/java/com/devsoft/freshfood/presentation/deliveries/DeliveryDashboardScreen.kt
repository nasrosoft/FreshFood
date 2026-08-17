package com.devsoft.freshfood.presentation.deliveries

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.freshfood.R
import com.devsoft.freshfood.domain.model.DeliveryOrderWithDetails
import com.devsoft.freshfood.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    var selectedFilterIndex by remember { mutableStateOf(0) }
    var selectedDeliveryDate by remember { mutableStateOf(LocalDate.now()) }
    var orderToDelete by remember { mutableStateOf<String?>(null) }
    
    val filterTabs = listOf(
        stringResource(R.string.all),
        stringResource(R.string.pending),
        stringResource(R.string.out_for_delivery),
        stringResource(R.string.delivered)
    )
    
    val context = LocalContext.current
    val isDriver = currentUserRole == "DELIVERY"

    LaunchedEffect(Unit) {
        viewModel.loadDeliveries()
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    fun parseDeliveryDate(timestamp: String?): LocalDate? {
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

    Scaffold(
        topBar = {
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)) {
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
                                if (isDriver) stringResource(R.string.driver_home) else stringResource(R.string.deliveries),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextDark
                            )
                        }

                        com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                    }

                    // Filter Tab Pills (Responsive Scrollable Row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterTabs.forEachIndexed { index, title ->
                            val isSelected = selectedFilterIndex == index
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) PrimaryBlue else CardSurfaceVariant)
                                    .clickable { selectedFilterIndex = index }
                                    .padding(horizontal = 14.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextMuted,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (uiState) {
                is DeliveryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
                }
                is DeliveryUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = (uiState as DeliveryUiState.Error).message,
                            color = StatusError
                        )
                    }
                }
                is DeliveryUiState.Success -> {
                    val allDeliveries = (uiState as DeliveryUiState.Success).deliveries
                    val roleDeliveries = if (isDriver) {
                        allDeliveries.filter { it.order.delivery_employee_id == currentUserId || it.order.delivery_employee_id == "00000000-0000-0000-0000-000000000000" }
                    } else {
                        allDeliveries
                    }

                    val filteredDeliveries = when (selectedFilterIndex) {
                        1 -> roleDeliveries.filter { it.order.status == "PENDING" || it.order.status == "ASSIGNED" }
                        2 -> roleDeliveries.filter { it.order.status == "OUT_FOR_DELIVERY" }
                        3 -> roleDeliveries.filter { 
                            it.order.status == "DELIVERED" && 
                            (parseDeliveryDate(it.order.updated_at ?: it.order.created_at) == selectedDeliveryDate) 
                        }
                        else -> roleDeliveries
                    }

                    val totalCount = roleDeliveries.size
                    val completedCount = roleDeliveries.count { it.order.status == "DELIVERED" }
                    val pendingCount = roleDeliveries.count { it.order.status != "DELIVERED" }
                    val nextDelivery = roleDeliveries.firstOrNull { it.order.status == "OUT_FOR_DELIVERY" || it.order.status == "ASSIGNED" || it.order.status == "PENDING" }
                    val totalCollected = roleDeliveries.filter { it.order.status == "DELIVERED" }.sumOf { 
                        it.items.sumOf { itemDetail -> (itemDetail.product?.selling_price ?: 0.0) * itemDetail.item.quantity } 
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Driver Mode Summary Cards (matching reference design)
                        if (isDriver) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = PrimaryBlue)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                stringResource(R.string.good_morning_driver), 
                                                color = Color.White, 
                                                fontWeight = FontWeight.Bold, 
                                                fontSize = 16.sp
                                            )
                                            Text("🚚", fontSize = 22.sp)
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceAround
                                        ) {
                                            DriverStatItem(count = "$totalCount", label = stringResource(R.string.total), color = Color.White)
                                            DriverStatItem(count = "$completedCount", label = stringResource(R.string.completed), color = StatusSuccessContainer)
                                            DriverStatItem(count = "$pendingCount", label = stringResource(R.string.pending), color = StatusWarningContainer)
                                        }
                                    }
                                }
                            }

                            // Next Delivery Highlight Card
                            if (nextDelivery != null) {
                                val nextAmount = nextDelivery.items.sumOf { (it.product?.selling_price ?: 0.0) * it.item.quantity }
                                item {
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
                                                Text(stringResource(R.string.next_delivery), fontWeight = FontWeight.Bold, color = TextDark)
                                                StatusBadge(status = nextDelivery.order.status)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(nextDelivery.customer?.name ?: stringResource(R.string.customers), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            nextDelivery.customer?.address?.let {
                                                Text(it, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "${String.format(java.util.Locale.US, "%,.0f", nextAmount)} DA",
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlue,
                                                    fontSize = 16.sp
                                                )
                                                Button(
                                                    onClick = { onDeliveryClick(nextDelivery.order.id) },
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text(stringResource(R.string.view_delivery))
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Today's Collection Card
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = StatusSuccessContainer)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(stringResource(R.string.todays_collection), style = MaterialTheme.typography.bodySmall, color = Color(0xFF065F46))
                                            Text(
                                                "${String.format(java.util.Locale.US, "%,.0f", totalCollected)} DA",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF047857)
                                            )
                                        }
                                        Text("💰", fontSize = 28.sp)
                                    }
                                }
                            }
                        }

                        // Date Navigator if Delivered tab is selected
                        if (selectedFilterIndex == 3) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CardSurface)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(onClick = { selectedDeliveryDate = selectedDeliveryDate.minusDays(1) }) {
                                            Icon(Icons.Filled.ArrowBack, contentDescription = "Previous Day")
                                        }
                                        Text(
                                            selectedDeliveryDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        IconButton(onClick = { selectedDeliveryDate = selectedDeliveryDate.plusDays(1) }) {
                                            Icon(Icons.Filled.ArrowForward, contentDescription = "Next Day")
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredDeliveries.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🚚", fontSize = 48.sp)
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(stringResource(R.string.no_deliveries_found), fontWeight = FontWeight.Bold, color = TextDark)
                                    }
                                }
                            }
                        } else {
                            items(filteredDeliveries) { delivery ->
                                DeliveryCardModern(
                                    delivery = delivery,
                                    onClick = { onDeliveryClick(delivery.order.id) },
                                    onDelete = { orderToDelete = delivery.order.id }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text(stringResource(R.string.delete_delivery_title)) },
            text = { Text(stringResource(R.string.delete_delivery_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeliveryOrder(orderToDelete!!)
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

@Composable
fun DeliveryCardModern(
    delivery: DeliveryOrderWithDetails,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val totalAmount = delivery.items.sumOf { (it.product?.selling_price ?: 0.0) * it.item.quantity }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "#DEL-${delivery.order.id.take(5).uppercase()}",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue,
                    fontSize = 13.sp
                )
                StatusBadge(status = delivery.order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(delivery.customer?.name ?: stringResource(R.string.customers), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
            
            delivery.driver?.let { driver ->
                val driverName = listOfNotNull(driver.first_name, driver.last_name).joinToString(" ").ifBlank { driver.email ?: "" }
                Text(
                    stringResource(R.string.driver_label, driverName),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = CardBorder)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.x_products, delivery.items.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                Text(
                    "${String.format(java.util.Locale.US, "%,.0f", totalAmount)} DA",
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun DriverStatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status) {
        "DELIVERED" -> Triple(StatusSuccessContainer, StatusSuccess, stringResource(R.string.delivered))
        "OUT_FOR_DELIVERY" -> Triple(StatusWarningContainer, StatusWarning, stringResource(R.string.out_for_delivery))
        "ASSIGNED" -> Triple(PrimaryBlueContainer, PrimaryBlue, stringResource(R.string.assigned))
        else -> Triple(CardSurfaceVariant, TextMuted, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}
