package com.devsoft.freshfood.presentation.dashboard

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.devsoft.freshfood.R
import com.devsoft.freshfood.domain.model.Customer
import com.devsoft.freshfood.domain.model.Product
import com.devsoft.freshfood.domain.model.Sale
import com.devsoft.freshfood.ui.theme.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel,
    onOpenDrawer: () -> Unit = {},
    onNavigateToSales: () -> Unit = {},
    onNavigateToDeliveries: () -> Unit = {}
) {
    val uiState by dashboardViewModel.uiState.collectAsState()

    var showNotificationAlerts by remember { mutableStateOf(false) }

    val todayDateFormatted = remember {
        try {
            LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))
        } catch (e: Exception) {
            "Today"
        }
    }

    val stockValue = remember(uiState.allProducts) {
        uiState.allProducts.sumOf { it.current_stock * it.purchase_price }
    }

    if (showNotificationAlerts) {
        AlertDialog(
            onDismissRequest = { showNotificationAlerts = false },
            title = { Text(stringResource(R.string.alerts), fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    val lowStockCount = uiState.lowStockProducts.size
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (lowStockCount > 0) StatusWarningContainer else StatusSuccessContainer)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (lowStockCount > 0) "⚠️" else "✅", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    stringResource(R.string.products_low_stock, lowStockCount),
                                    fontWeight = FontWeight.Bold,
                                    color = if (lowStockCount > 0) StatusWarning else StatusSuccess
                                )
                                Text(
                                    if (lowStockCount > 0) stringResource(R.string.low_stock_details) else stringResource(R.string.all_in_stock),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("🚚", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    stringResource(R.string.todays_deliveries),
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    "${stringResource(R.string.pending)}: ${uiState.pendingDeliveriesToday} | ${stringResource(R.string.completed)}: ${uiState.completedDeliveriesToday}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationAlerts = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            // Modern Header Bar from reference design
            Surface(color = CardSurface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = PrimaryBlueDark)
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${stringResource(R.string.good_morning)}, ${stringResource(R.string.admin)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("👋", fontSize = 16.sp)
                            }
                            Text(
                                todayDateFormatted,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        com.devsoft.freshfood.presentation.components.GlobalSyncButton()
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlueContainer)
                                .clickable { showNotificationAlerts = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Alerts", tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        containerColor = AppBackground
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Top 3 Metric Cards (Sales, Profit, Customer Credit)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MetricCard(
                            title = stringResource(R.string.todays_sales),
                            value = "${String.format(Locale.US, "%,.0f", uiState.salesTotal)} DA",
                            badgeText = stringResource(R.string.vs_yesterday),
                            badgeColor = StatusSuccess,
                            badgeContainer = StatusSuccessContainer,
                            onClick = { dashboardViewModel.showDetail(DashboardDetailType.SALES) }
                        )

                        MetricCard(
                            title = "${stringResource(R.string.profit_day)} (${stringResource(uiState.timeRange.stringRes)})",
                            value = "${String.format(Locale.US, "%,.0f", uiState.profitTotal)} DA",
                            badgeText = stringResource(R.string.est_margin),
                            badgeColor = PrimaryBlue,
                            badgeContainer = PrimaryBlueContainer,
                            onClick = { dashboardViewModel.showDetail(DashboardDetailType.PROFIT) }
                        )

                        MetricCard(
                            title = stringResource(R.string.customer_credit),
                            value = "${String.format(Locale.US, "%,.0f", uiState.totalCredit)} DA",
                            badgeText = stringResource(R.string.overview),
                            badgeColor = StatusWarning,
                            badgeContainer = StatusWarningContainer,
                            onClick = { dashboardViewModel.showDetail(DashboardDetailType.CREDIT) }
                        )
                    }

                    // 2. Stock Value Card (matching reference design)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(stringResource(R.string.stock_value), style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${String.format(Locale.US, "%,.0f", stockValue)} DA",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(PrimaryBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.List, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(26.dp))
                            }
                        }
                    }

                    // 3. Alerts Section Card (Expiring, Low Stock, Expired)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.alerts), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            AlertRow(
                                icon = "⚠️",
                                text = stringResource(R.string.products_low_stock, uiState.lowStockProducts.size),
                                color = StatusWarning,
                                onClick = { dashboardViewModel.showDetail(DashboardDetailType.LOW_STOCK) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AlertRow(
                                icon = "⏳",
                                text = stringResource(R.string.products_expiring_soon),
                                color = StatusWarning,
                                onClick = { dashboardViewModel.showDetail(DashboardDetailType.LOW_STOCK) }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            AlertRow(
                                icon = "🔴",
                                text = stringResource(R.string.check_batch_expiration),
                                color = StatusError,
                                onClick = { dashboardViewModel.showDetail(DashboardDetailType.LOW_STOCK) }
                            )
                        }
                    }

                    // 4. Today's Deliveries Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        onClick = onNavigateToDeliveries
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(stringResource(R.string.todays_deliveries), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("🚚", fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                DeliveryCounterItem(count = "${uiState.totalDeliveriesToday}", label = stringResource(R.string.total), color = PrimaryBlue)
                                DeliveryCounterItem(count = "${uiState.completedDeliveriesToday}", label = stringResource(R.string.completed), color = StatusSuccess)
                                DeliveryCounterItem(count = "${uiState.pendingDeliveriesToday}", label = stringResource(R.string.pending), color = StatusWarning)
                            }
                        }
                    }

                    // 5. Sales Overview Interactive Line Chart
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
                                Text(stringResource(R.string.sales_overview), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                
                                // Time Range Selector Filter
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    DashboardTimeRange.values().forEach { range ->
                                        val isSelected = uiState.timeRange == range
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) PrimaryBlue else Color.Transparent)
                                                .clickable { dashboardViewModel.setTimeRange(range) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                stringResource(range.stringRes),
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else TextMuted
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Smooth Line Chart Canvas
                            SalesCurveChart(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Detail Dialogs
            when (uiState.selectedDetailType) {
                DashboardDetailType.SALES -> {
                    SalesDetailDialog(
                        timeRange = uiState.timeRange,
                        sales = uiState.filteredSales,
                        customers = uiState.allCustomers,
                        totalAmount = uiState.salesTotal,
                        onDismiss = { dashboardViewModel.hideDetail() }
                    )
                }
                DashboardDetailType.PROFIT -> {
                    ProfitDetailDialog(
                        timeRange = uiState.timeRange,
                        sales = uiState.filteredSales,
                        revenueTotal = uiState.salesTotal,
                        profitTotal = uiState.profitTotal,
                        onDismiss = { dashboardViewModel.hideDetail() }
                    )
                }
                DashboardDetailType.CREDIT -> {
                    CustomerCreditDetailDialog(
                        customers = uiState.allCustomers.filter { it.current_credit > 0 },
                        totalCredit = uiState.totalCredit,
                        onDismiss = { dashboardViewModel.hideDetail() }
                    )
                }
                DashboardDetailType.LOW_STOCK -> {
                    LowStockDetailDialog(
                        products = uiState.lowStockProducts,
                        onDismiss = { dashboardViewModel.hideDetail() }
                    )
                }
                DashboardDetailType.NONE -> {}
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    badgeText: String,
    badgeColor: Color,
    badgeContainer: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = TextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(badgeText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
            }
        }
    }
}

@Composable
private fun AlertRow(
    icon: String,
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ArrowForward, contentDescription = null, tint = TextSubtle, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun DeliveryCounterItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = TextMuted)
    }
}

@Composable
private fun SalesCurveChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val points = listOf(
            Offset(0f, height * 0.7f),
            Offset(width * 0.2f, height * 0.5f),
            Offset(width * 0.4f, height * 0.8f),
            Offset(width * 0.6f, height * 0.35f),
            Offset(width * 0.8f, height * 0.45f),
            Offset(width, height * 0.2f)
        )

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val current = points[i]
                val midX = (prev.x + current.x) / 2f
                cubicTo(midX, prev.y, midX, current.y, current.x, current.y)
            }
        }

        // Gradient under the curve
        val fillPath = Path().apply {
            addPath(path)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(PrimaryBlue.copy(alpha = 0.25f), PrimaryBlue.copy(alpha = 0.0f))
            )
        )

        // Draw curve line
        drawPath(
            path = path,
            color = PrimaryBlue,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        for (point in points) {
            drawCircle(color = CardSurface, radius = 5.dp.toPx(), center = point)
            drawCircle(color = PrimaryBlue, radius = 3.dp.toPx(), center = point)
        }
    }
}

// -------------------------------------------------------------
// DETAIL DIALOGS
// -------------------------------------------------------------

@Composable
fun SalesDetailDialog(
    timeRange: DashboardTimeRange,
    sales: List<Sale>,
    customers: List<Customer>,
    totalAmount: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stringResource(R.string.sales_details)} (${stringResource(timeRange.stringRes)})", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Sales", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                "${String.format(Locale.US, "%,.1f", totalAmount)} DA", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Transactions", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Text(
                                "${sales.size}", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (sales.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No sales in this period.", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sales) { sale ->
                            val customerName = customers.find { it.id == sale.customer_id }?.name ?: "Direct Sale"
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            sale.invoice_number ?: "Sale", 
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            "${sale.total_amount} DA", 
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlue
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            customerName, 
                                            style = MaterialTheme.typography.bodySmall, 
                                            color = TextMuted
                                        )
                                        Text(
                                            sale.payment_method, 
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = StatusSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun ProfitDetailDialog(
    timeRange: DashboardTimeRange,
    sales: List<Sale>,
    revenueTotal: Double,
    profitTotal: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${stringResource(R.string.profit_details)} (${stringResource(timeRange.stringRes)})", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PrimaryBlueContainer)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Revenue", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${String.format(Locale.US, "%,.1f", revenueTotal)} DA", 
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Profit Margin", style = MaterialTheme.typography.bodyMedium)
                            Text("20.0%", fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Estimated Net Profit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "${String.format(Locale.US, "%,.1f", profitTotal)} DA", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Transactions contributing: ${sales.size}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sales) { sale ->
                        val itemProfit = sale.total_amount * 0.20
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(sale.invoice_number ?: "Sale", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text("Revenue: ${sale.total_amount} DA", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Profit", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    Text(
                                        "+${String.format(Locale.US, "%,.1f", itemProfit)} DA", 
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun CustomerCreditDetailDialog(
    customers: List<Customer>,
    totalCredit: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Customer Credit Details", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusWarningContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Outstanding Credit", style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
                            Text(
                                "${String.format(Locale.US, "%,.1f", totalCredit)} DA", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Debtors", style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
                            Text(
                                "${customers.size}", 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (customers.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No outstanding customer credits!", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(customers.sortedByDescending { it.current_credit }) { customer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(customer.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        customer.phone?.let { phone ->
                                            Text(phone, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                        }
                                    }
                                    Text(
                                        "${String.format(Locale.US, "%,.0f", customer.current_credit)} DA", 
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = StatusWarning
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
fun LowStockDetailDialog(
    products: List<Product>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Low Stock Products", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusErrorContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Products below threshold", style = MaterialTheme.typography.bodyMedium, color = StatusError)
                        Text(
                            "${products.size} items", 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = StatusError
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (products.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("All products have healthy stock levels!", color = TextMuted)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(products.sortedBy { it.current_stock }) { product ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = CardSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            "Selling: ${product.selling_price} DA", 
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "${product.current_stock} / ${product.min_stock} ${product.unit}",
                                            fontWeight = FontWeight.Bold,
                                            color = if (product.current_stock <= 0) StatusError else StatusWarning
                                        )
                                        Text("Stock / Min", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
