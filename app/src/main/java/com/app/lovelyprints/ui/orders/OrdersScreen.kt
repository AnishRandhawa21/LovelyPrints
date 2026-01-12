package com.app.lovelyprints.ui.orders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.data.model.Order
import com.app.lovelyprints.viewmodel.OrdersViewModel
import com.app.lovelyprints.viewmodel.OrdersViewModelFactory

@Composable
fun OrdersScreen(viewModelFactory: OrdersViewModelFactory) {

    val viewModel: OrdersViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Current Orders", "History")

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.loadOrders() }) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                val orders =
                    if (selectedTab == 0) uiState.currentOrders else uiState.orderHistory

                if (orders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (selectedTab == 0)
                                "No current orders"
                            else
                                "No order history",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(order)
                        }
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*                                   CARD                                     */
/* -------------------------------------------------------------------------- */

@Composable
fun OrderCard(order: Order) {
    val status = order.status
    val paymentStatus = if (order.isPaid) "paid" else "pending"
    val amountText = "₹${order.totalPrice}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            /* HEADER */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val ordernum = "${order.orderNo}"
                Text("Order #$ordernum", style = MaterialTheme.typography.titleMedium)
                OrderStatusChip(status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            /* SHOP */
            order.shop?.shopName?.let { shop ->
                Text("Shop: $shop", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
            }

            /* DESCRIPTION / NOTES */
            Text(
                text = order.notes ?: "No notes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            /* PRICE + PAYMENT */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Amount: $amountText", style = MaterialTheme.typography.bodyLarge)
                PaymentStatusChip(paymentStatus)
            }

            /* DOCUMENT */
            order.documents?.firstOrNull()?.let { doc ->

                val fileName = doc.fileName ?: "Document"
                val pages = doc.pageCount ?: 0
                val copies = doc.copies ?: 1

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "$fileName • $pages pages • $copies ${if (copies > 1) "copies" else "copy"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*                                   CHIPS                                    */
/* -------------------------------------------------------------------------- */

@Composable
fun OrderStatusChip(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "processing" -> MaterialTheme.colorScheme.primary
        "completed" -> MaterialTheme.colorScheme.secondary
        "cancelled" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun PaymentStatusChip(status: String) {
    val color = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.tertiary
        "paid" -> MaterialTheme.colorScheme.secondary
        "failed" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
