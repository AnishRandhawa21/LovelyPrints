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
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.painterResource
import com.app.lovelyprints.data.model.isExpired
import com.app.lovelyprints.theme.AlmostBlack // ADDED: Import theme colors
import com.app.lovelyprints.theme.Cream // ADDED: Import theme colors
import com.app.lovelyprints.theme.DeepAmber // ADDED: Import theme colors
import com.app.lovelyprints.theme.GoldenYellow // ADDED: Import theme colors
import com.app.lovelyprints.theme.LimeGreen
import com.app.lovelyprints.theme.MediumGray // ADDED: Import theme colors
import com.app.lovelyprints.theme.OffWhite // ADDED: Import theme colors
import com.app.lovelyprints.theme.SoftPink
import androidx.compose.ui.zIndex
import com.app.lovelyprints.R
import com.app.lovelyprints.theme.CoralRed
import com.app.lovelyprints.theme.DarkGreen
import com.app.lovelyprints.theme.SoftBlue


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
        color = Cream // CHANGED: from Color(0xFF151419) to Cream
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
                    containerColor = Color.White, // CHANGED: from Color(0xFF363636) to Color.White
                    color = SoftPink // CHANGED: from Color(0xFFFF9500) to GoldenYellow
                )
            }
        ) {

            Column(
                modifier = Modifier.padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 0.dp) // CHANGED: Added padding control
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Orders",
                        style = MaterialTheme.typography.headlineLarge.copy( // CHANGED: Added font weight
                            fontWeight = FontWeight.Bold
                        ),
                        fontFamily = Inter,
                        color = AlmostBlack // CHANGED: from Color(0xFF878787) to AlmostBlack
                    )
                }

                Surface(
                    shadowElevation = 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, AlmostBlack.copy(alpha = 0.6f)),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Box {
                        // Animated sliding indicator - BEHIND the tabs
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = AlmostBlack,
                            divider = {},
                            indicator = { tabPositions ->
                                Box(
                                    modifier = Modifier
                                        .tabIndicatorOffset(tabPositions[selectedTab])
                                        .fillMaxHeight()
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AlmostBlack)
                                        .zIndex(-1f) // Put it BEHIND the text
                                )
                            }
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = selectedTab == index

                                Tab(
                                    selected = isSelected,
                                    onClick = { selectedTab = index },
                                    modifier = Modifier.zIndex(1f), // Put text in FRONT
                                    text = {
                                        Text(
                                            title,
                                            color = if (isSelected) Color.White else AlmostBlack.copy(alpha = 0.6f),
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
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
                            contentPadding = PaddingValues(vertical = 16.dp), // CHANGED: only vertical padding
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
                                color = CoralRed // CHANGED: using error red color
                            )
                        }
                    }

                    orders.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {

                            Image(
                                painter = painterResource(id = R.drawable.no_order),
                                contentDescription = "NO_Order",
                            )
//                            Text(
//                                text =
//                                    if (selectedTab == 0)
//                                        "No current orders"
//                                    else
//                                        "No order history",
//                                color = MediumGray // CHANGED: from Color.Gray to MediumGray
//                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 16.dp), // CHANGED: only vertical padding
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = orders,
                                key = { it.id }
                            ) { order ->
                                ExpandableOrderCard(
                                    order = order,
                                    expanded = expandedOrderId == order.id,
                                    isHistory = selectedTab == 1,
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
        color = Color(0xFF1F5A3D) // KEPT: Green color for OTP box (good for contrast)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Pickup Code",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFB6EACB) // KEPT: Light green for text
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
                color = Color(0xFFB6EACB), // KEPT: Light green for text
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
    isHistory: Boolean,
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


    val interactionSource = remember { MutableInteractionSource() }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null // 🔥 disables gray press layer
            ) {
                onClick()
            },
        colors = CardDefaults.cardColors(
            containerColor = Color.White // CHANGED: from Color(0xFF363636) to Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (expanded) 4.dp else 2.dp // CHANGED: 2.dp to 4.dp
        ),
        shape = RoundedCornerShape(16.dp) // CHANGED: Added rounded corners
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
                        style = MaterialTheme.typography.titleMedium.copy( // CHANGED: Added bold
                            fontWeight = FontWeight.Bold
                        ),
                        color = AlmostBlack // CHANGED: from Color.White to AlmostBlack
                    )

                    Text(
                        text = "ID: #${order.id.lastSix()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumGray // CHANGED: from Color(0xFF9E9E9E) to MediumGray
                    )
                    order.shop?.shopName?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = AlmostBlack // CHANGED: from Color(0xFFFFFFFF) to AlmostBlack
                        )
                    }

                    formatOrderDate(order.createdAt)?.let {
                        Text(
                            text = "Placed on $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray // CHANGED: from Color(0xFF9E9E9E) to MediumGray
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
                        tint = AlmostBlack, // CHANGED: from Color.White to AlmostBlack
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
                        HorizontalDivider(color = MediumGray.copy(alpha = 0.3f)) // CHANGED: from Color(0xFF878787) to MediumGray with transparency
                        Spacer(Modifier.height(12.dp))

                        //Description

                        Column {

                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.labelMedium,
                                color = AlmostBlack // CHANGED: from Color(0xFFFFFFFF) to AlmostBlack
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = order.notes ?: "No notes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MediumGray, // CHANGED: from Color(0xFFB0B0B0) to MediumGray
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
                                    color = AlmostBlack // CHANGED: from Color.White to AlmostBlack
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                StrokedText(
                                    text = "₹${order.totalPrice}",
                                    textColor = AlmostBlack, // CHANGED: from Color(0xFFFBFFFD) to AlmostBlack
                                    strokeColor = DarkGreen, // CHANGED: from Color(0xFF1D6A2B) to GoldenYellow
                                    strokeWidth = 2.5f
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
                            HorizontalDivider(color = MediumGray.copy(alpha = 0.3f)) // CHANGED: from Color(0xFF878787)
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
                                color = MediumGray // CHANGED: from Color(0xFFCCCCCC) to MediumGray
                            )
                        }
                    }
                    // ================= HISTORY BILL =================

                    if (isHistory) {

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MediumGray.copy(alpha = 0.3f)) // CHANGED: from Color(0xFF878787)
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

        "expired" ->
            Color(0xFFD32F2F) // strong red

        "pending" ->
            Color(0xFFF9A825) // amber / yellow

        "confirmed" ->
            Color(0xFF1976D2) // BLUE (confirmation / success)

        "processing" ->
            Color(0xFF0288D1) // lighter blue (in progress)

        "ready" ->
            Color(0xFF388E3C) // GREEN (ready to go)

        "completed" ->
            Color(0xFF2E7D32) // darker green (final state)

        "cancelled" ->
            Color(0xFFD32F2F) // deep red

        else ->
            Color(0xFF757575) // neutral gray
    }


    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp) // CHANGED: from MaterialTheme.shapes.small to specific value
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
        "paid" -> Color(0xFF689F38) // KEPT: Green for paid
        "pending" -> DeepAmber // CHANGED: from Color(0xFF9C5A10) to DeepAmber
        "failed" -> Color(0xFF6E2B2B) // KEPT: Red for failed
        else -> MediumGray // CHANGED: from Color(0xFF4F4F4F) to MediumGray
    }

    Surface(
        color = color,
        shape = RoundedCornerShape(8.dp) // CHANGED: from MaterialTheme.shapes.small to specific value
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
            containerColor = OffWhite // CHANGED: from Color(0xFF1E1E1E) to Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp) // CHANGED: Added elevation
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
            MediumGray.copy(alpha = 0.5f), // CHANGED: from Color(0xFF2A2A2A) to OffWhite
            OffWhite.copy(alpha = 0.2f), // CHANGED: from Color(0xFF3A3A3A) to MediumGray
            MediumGray.copy(alpha = 0.5f) // CHANGED: from Color(0xFF2A2A2A) to OffWhite
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
            color = AlmostBlack // CHANGED: from Color.White to AlmostBlack
        )

        Text(
            text = value,
            style = if (bold)
                MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            else
                MaterialTheme.typography.bodySmall,
            color = AlmostBlack // CHANGED: from Color.White to AlmostBlack
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
            style = MaterialTheme.typography.labelMedium.copy( // CHANGED: Added bold
                fontWeight = FontWeight.Bold
            ),
            color = AlmostBlack // CHANGED: from Color.White to AlmostBlack
        )

        Spacer(Modifier.height(8.dp))

        documents.forEach { doc ->

            val pages = doc.pageCount ?: 0
            val copies = doc.copies ?: 1

            val subtotal =
                pages * copies * pricePerPage

            Text(
                text = doc.fileName ?: "Document",
                color = Color(0xFF1976D2), // CHANGED: from Color(0xFFFF9500) to GoldenYellow
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium // CHANGED: Added medium weight
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
            color = MediumGray.copy(alpha = 0.3f) // CHANGED: from Color(0xFF555555) to MediumGray with transparency
        )

        Spacer(Modifier.height(8.dp))

        BillRow(
            title = "Total",
            value = "₹${order.totalPrice}",
            bold = true
        )
    }
}