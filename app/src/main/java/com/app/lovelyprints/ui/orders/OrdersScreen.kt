package com.app.lovelyprints.ui.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.app.lovelyprints.data.model.lastSix
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import com.app.lovelyprints.data.model.isExpired


/* -------------------------------------------------------------------------- */
/*                                   SCREEN                                   */
/* -------------------------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModelFactory: OrdersViewModelFactory
) {

    val viewModel: OrdersViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(key1 = true) {
        if (uiState.currentOrders.isEmpty() &&
            uiState.orderHistory.isEmpty()
        ) {
            viewModel.loadOrders()
        }
    }


    val pullToRefreshState = rememberPullToRefreshState()

    var selectedTab by remember { mutableStateOf(0) }
    var expandedOrderId by remember { mutableStateOf<String?>(null) }

    val tabs = listOf("Current Orders", "History")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF151419)
    ) {

        PullToRefreshBox(
            isRefreshing = uiState.isLoading,
            onRefresh = { viewModel.loadOrders() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = uiState.isLoading,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = Color(0xFF363636),
                    color = Color(0xFFFF9500)
                )
            }
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
                                        else Color(0xFFCECECE)
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

                    uiState.isLoading &&
                            uiState.currentOrders.isEmpty() &&
                            uiState.orderHistory.isEmpty() -> {

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(6) {
                                SkeletonOrderCard()
                            }
                        }
                    }

                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    orders.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text =
                                    if (selectedTab == 0)
                                        "No current orders"
                                    else
                                        "No order history",
                                color = Color.Gray
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = orders,
                                key = { it.id }
                            ) { order ->
                                ExpandableOrderCard(
                                    order = order,
                                    expanded = expandedOrderId == order.id,
                                    isHistory = selectedTab == 1, // 👈 THIS LINE
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
    isHistory: Boolean,   // 👈 ADD THIS
    onClick: () -> Unit
)
 {

    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "arrow_rotation"
    )
     val alpha by animateFloatAsState(
         targetValue = if (order.isExpired()) 0.55f else 1f,
         label = "expired_alpha"
     )


     Card(
        modifier = Modifier
            .fillMaxWidth()
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

                    Text(
                        text = "ID: #${order.id.lastSix()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9E9E9E)
                    )
                    order.shop?.shopName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFFFFF)
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
                    OrderStatusChip(
                        if (order.isExpired()) "expired" else order.status
                    )


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

                enter =
                    fadeIn(
                        animationSpec = tween(
                            durationMillis = 200,
                            easing = LinearOutSlowInEasing
                        )
                    ) +
                            expandVertically(
                                animationSpec = tween(
                                    durationMillis = 360,
                                    easing = FastOutSlowInEasing
                                )
                            ),

                exit =
                    fadeOut(
                        animationSpec = tween(
                            durationMillis = 180,
                            easing = FastOutLinearInEasing
                        )
                    ) +
                            shrinkVertically(
                                animationSpec = tween(
                                    durationMillis = 380,
                                    easing = FastOutLinearInEasing
                                )
                            )
            )
            {
                Column {

                    if(!isHistory) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color(0xFF878787))
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
                    }


                    Spacer(Modifier.height(10.dp))
                    if (!isHistory) {
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
                    }
                    // ================= OTP (READY STATE) =================

                    AnimatedVisibility(
                        visible = expanded &&
                                order.status == "ready" &&
                                !order.isExpired() &&
                                !order.otpVerified &&
                                !order.deliveryOtp.isNullOrBlank(),
                                enter = fadeIn(),
                        exit = fadeOut()
                    )
                    {

                        Column {

                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFF878787))
                            Spacer(Modifier.height(12.dp))

                            PickupOtpBox(
                                otp = order.deliveryOtp!!
                            )
                        }
                    }
                    if (!isHistory) {

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
                    // ================= HISTORY BILL =================

                    if (isHistory) {

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFF878787))
                        Spacer(Modifier.height(12.dp))

                        OrderBillSection(order)
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
        "expired" -> Color(0xFFB3261E)

        "pending" -> Color(0xFF9C5A10)

        "confirmed" -> Color(0xFF2E4A59)

        "processing" -> Color(0xFF2E4A59)

        "ready" -> Color(0xFF1F5A3D)

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

@Composable
fun SkeletonOrderCard() {

    val shimmer = rememberShimmerBrush()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Column {
                Box(
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )

                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.35f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }

            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(80.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmer)
            )
        }
    }
}
@Composable
fun rememberShimmerBrush(): Brush {

    val transition = rememberInfiniteTransition(label = "shimmer")

    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            )
        ),
        label = "x"
    )

    return Brush.linearGradient(
        colors = listOf(
            Color(0xFF2A2A2A),
            Color(0xFF3A3A3A),
            Color(0xFF2A2A2A)
        ),
        start = Offset(x - 300f, 0f),
        end = Offset(x, 600f)
    )
}

//Billing system

@Composable
fun BillRow(
    title: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = if (bold)
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodySmall,
            color = Color.White
        )

        Text(
            text = value,
            style = if (bold)
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}

//Bill breakdown composable
@Composable
fun OrderBillSection(order: Order) {

    val documents = order.documents ?: return

    // Total printed pages across all documents
    val totalPages =
        documents.sumOf { doc ->
            (doc.pageCount ?: 0) * (doc.copies ?: 1)
        }

    // Derive per-page price
    val pricePerPage =
        if (totalPages == 0) 0.0
        else order.totalPrice.toDouble() / totalPages

    Column {

        Text(
            text = "Bill Summary",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        documents.forEach { doc ->

            val pages = doc.pageCount ?: 0
            val copies = doc.copies ?: 1

            val subtotal =
                pages * copies * pricePerPage

            Text(
                text = doc.fileName ?: "Document",
                color = Color(0xFFFF9500),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(4.dp))

            BillRow(
                title = "$pages pages × ₹${"%.2f".format(pricePerPage)} × $copies copies",
                value = "₹${"%.2f".format(subtotal)}"
            )

            Spacer(Modifier.height(8.dp))
        }

        HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFF555555)
        )

        Spacer(Modifier.height(8.dp))

        BillRow(
            title = "Total",
            value = "₹${order.totalPrice}",
            bold = true
        )
    }
}


