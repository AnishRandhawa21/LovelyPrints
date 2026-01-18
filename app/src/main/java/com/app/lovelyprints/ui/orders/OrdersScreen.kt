package com.app.lovelyprints.ui.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.data.model.Order
import com.app.lovelyprints.theme.Inter
import com.app.lovelyprints.utils.formatOrderDate
import com.app.lovelyprints.viewmodel.OrdersViewModel
import com.app.lovelyprints.viewmodel.OrdersViewModelFactory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween


/* -------------------------------------------------------------------------- */
/*                                   SCREEN                                   */
/* -------------------------------------------------------------------------- */

@Composable
fun OrdersScreen(
    viewModelFactory: OrdersViewModelFactory
) {

    val viewModel: OrdersViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("Current Orders", "History")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF151419)
    ) {
        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Orders",
                    style = MaterialTheme.typography.headlineLarge,
                    fontFamily = Inter,
                    color = Color(0xFF878787)
                )

                val infiniteTransition = rememberInfiniteTransition(label = "refresh")

                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(900)
                    ),
                    label = "rotation"
                )

                IconButton(
                    onClick = { viewModel.loadOrders() },
                    enabled = !uiState.isLoading
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh orders",
                        tint = Color(0xFFFF9500),
                        modifier =
                            if (uiState.isLoading)
                                Modifier.rotate(rotation)
                            else
                                Modifier
                    )
                }
            }

            Surface(
                shadowElevation = 6.dp,
                shape = RoundedCornerShape(15.dp),
                border = BorderStroke(1.dp, Color(0xFFFF9500)),
                color = Color(0xFF363636),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF878787),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFFFF9500)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    title,
                                    color = if (selectedTab == index)
                                        Color(0xFFFF9500)
                                    else
                                        Color(0xFFCECECE)
                                )
                            }
                        )
                    }
                }

            }

            val orders =
                if (selectedTab == 0)
                    uiState.currentOrders
                else
                    uiState.orderHistory

            when {

                // ---------------- LOADING ----------------
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF9500),
                            strokeWidth = 3.dp
                        )
                    }
                }

                // ---------------- ERROR ----------------
                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loadOrders() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9500)
                            )
                        ) {
                            Text("Retry")
                        }

                    }
                }

                // ---------------- EMPTY ----------------
                orders.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                if (selectedTab == 0)
                                    "No current orders"
                                else
                                    "No order history",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.loadOrders() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9500)
                            )
                        ) {
                            Text(
                                text = "Refresh",
                                color = Color.White
                            )
                        }
                    }
                }


                // ---------------- CONTENT ----------------
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = orders,
                            key = { order -> order.id }
                        ) { order ->

                            ExpandableOrderCard(
                                order = order,
                                expanded = expandedOrderId == order.id,
                                onClick = {
                                    expandedOrderId =
                                        if (expandedOrderId == order.id) null
                                        else order.id
                                }
                            )
                        }
                    }
                }
            }


        }
    }
}
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFFF9500),
            strokeWidth = 3.dp
        )
    }
}
/* -------------------------------------------------------------------------- */
/*                              OTP                                           */
/* -------------------------------------------------------------------------- */

@Composable
fun PickupOtpBox(otp: String) {

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1F5A3D)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Pickup Code",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB6EACB)
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = otp,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 3.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "Show this code at the shop counter to collect your prints",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFB6EACB),
                textAlign = TextAlign.Center
            )
        }
    }
}

/* -------------------------------------------------------------------------- */
/*                              EXPANDABLE CARD                                */
/* -------------------------------------------------------------------------- */

@Composable
fun StrokedText(
    text: String,
    textColor: Color,
    strokeColor: Color,
    strokeWidth: Float,
    fontSize: TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    val strokeStyle = remember(strokeColor, strokeWidth, fontSize, fontWeight) {
        TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = strokeColor,
            drawStyle = Stroke(
                width = strokeWidth,
                join = StrokeJoin.Round
            )
        )
    }

    val fillStyle = remember(textColor, fontSize, fontWeight) {
        TextStyle(
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = textColor
        )
    }

    Box {
        Text(text = text, style = strokeStyle)
        Text(text = text, style = fillStyle)
    }
}

@Composable
fun ExpandableOrderCard(
    order: Order,
    expanded: Boolean,
    onClick: () -> Unit
) {

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF363636)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 8.dp else 2.dp
        )
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            /* ---------------- COLLAPSED CONTENT ---------------- */

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column {
                    Text(
                        text = "Order #${order.orderNo}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )

                    order.shop?.shopName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBDBDBD)
                        )
                    }

                    formatOrderDate(order.createdAt)?.let {
                        Text(
                            text = "Placed on $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9E9E9E)
                        )
                    }
                }


                Row(verticalAlignment = Alignment.CenterVertically) {
                    OrderStatusChip(order.status)

                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .rotate(rotation)
                    )
                }
            }

            /* ---------------- EXPANDED CONTENT ---------------- */

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {

                Column {

                    Spacer(Modifier.height(12.dp))
                    Divider(color = Color(0xFF878787))
                    Spacer(Modifier.height(12.dp))

                    //Description

                    Column {

                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFFFFFF)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = order.notes ?: "No notes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB0B0B0),
                            lineHeight = 18.sp
                        )
                    }


                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        //AMOUNT

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Amount:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            StrokedText(
                                text = "₹${order.totalPrice}",
                                textColor = Color(0xFFFBFFFD),
                                strokeColor = Color(0xFF1D6A2B),
                                strokeWidth = 5.5f
                            )
                        }

                        PaymentStatusChip(
                            if (order.isPaid) "paid" else "pending"
                        )
                    }
                    // ================= OTP (READY STATE) =================

                    AnimatedVisibility(
                        visible =
                            expanded &&
                                    order.status == "ready" &&
                                    !order.otpVerified &&
                                    !order.deliveryOtp.isNullOrBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {

                        Column {

                            Spacer(Modifier.height(12.dp))
                            Divider(color = Color(0xFF878787))
                            Spacer(Modifier.height(12.dp))

                            PickupOtpBox(
                                otp = order.deliveryOtp!!
                            )
                        }
                    }


                    order.documents?.firstOrNull()?.let { doc ->

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text =
                                "${doc.fileName ?: "Document"} • " +
                                        "${doc.pageCount ?: 0} pages • " +
                                        "${doc.copies ?: 1} copies",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                }
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
        "pending" -> Color(0xFF9C5A10)
        "processing" -> Color(0xFF2E4A59)
        "completed" -> Color(0xFF1F5A3D)
        "cancelled" -> Color(0xFF6E2B2B)
        else -> Color(0xFF4F4F4F)
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
fun PaymentStatusChip(status: String) {

    val color = when (status.lowercase()) {
        "paid" -> Color(0xFF1F5A3D)
        "pending" -> Color(0xFF9C5A10)
        "failed" -> Color(0xFF6E2B2B)
        else -> Color(0xFF4F4F4F)
    }

    Surface(
        color = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
