package com.app.lovelyprints.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.R
import com.app.lovelyprints.data.model.Shop
import com.app.lovelyprints.theme.Inter
import com.app.lovelyprints.viewmodel.HomeViewModel
import com.app.lovelyprints.viewmodel.HomeViewModelFactory
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.app.lovelyprints.firebase.registerFcmToken
import com.app.lovelyprints.theme.AlmostBlack
import com.app.lovelyprints.theme.Bebasneue
import com.app.lovelyprints.theme.CoralRed
import com.app.lovelyprints.theme.Cream
import com.app.lovelyprints.theme.DarkGreen
import com.app.lovelyprints.theme.DeepAmber
import com.app.lovelyprints.theme.GoldenYellow
import com.app.lovelyprints.theme.GradientPinkEnd
import com.app.lovelyprints.theme.GradientPinkStart
import com.app.lovelyprints.theme.GradientPurpleEnd
import com.app.lovelyprints.theme.GradientPurpleStart
import com.app.lovelyprints.theme.LimeGreen
import com.app.lovelyprints.theme.MediumGray
import com.app.lovelyprints.theme.Montserrat
import com.app.lovelyprints.theme.OffWhite
import com.app.lovelyprints.theme.PastelBlue
import com.app.lovelyprints.theme.PastelCoral
import com.app.lovelyprints.theme.PastelLavender
import com.app.lovelyprints.theme.SoftPink
import com.app.lovelyprints.theme.SoftYellow
import com.app.lovelyprints.theme.Thunder
import com.app.lovelyprints.theme.White




// --------------------------------------------------
// 🏠 HOME SCREEN
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModelFactory: HomeViewModelFactory,
    tokenManager: com.app.lovelyprints.core.auth.TokenManager,
    notificationApi: com.app.lovelyprints.data.api.NotificationApi,
    onShopClick: (String) -> Unit
) {

    LaunchedEffect(Unit) {
        val userId = tokenManager.getUserIdBlocking()
        if (!userId.isNullOrBlank()) {
            registerFcmToken(userId, notificationApi)
        }
    }
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

//    val currentHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
//    val isShopOpen = currentHour in 9..16

    var searchQuery by remember { mutableStateOf("") }

    // ✅ SEARCH FILTER
    val filteredShops = remember(searchQuery, uiState.shops) {

        val result =
            if (searchQuery.isBlank()) {
                uiState.shops
            } else {
                uiState.shops.filter {
                    it.shopName.contains(searchQuery, ignoreCase = true) ||
                            it.block.contains(searchQuery, ignoreCase = true)
                }
            }

        result.sortedBy { !it.isActive }
    }

    val isSearching = searchQuery.isNotBlank()
    val noSearchResults = isSearching && filteredShops.isEmpty()

    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.loadShops() },
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = uiState.isLoading,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                color = LimeGreen
            )
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Cream)
                .padding(horizontal = 16.dp)
        )
        {

            Text(
                text = "Available Shops",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                fontFamily = Inter,
                color = AlmostBlack
            )

            Spacer(Modifier.height(12.dp))

            FancySearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClearClick = { searchQuery = "" }
            )

            Spacer(Modifier.height(16.dp))

            when {
                uiState.isLoading -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(6) { SkeletonShopCard() }
                    }
                }

                noSearchResults -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.noshop), // 👈 new image
                            contentDescription = "No shops found"
                        )
                    }
                }

                uiState.shops.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.connectionlost),
                            contentDescription = "No internet"
                        )
                    }
                }


                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        itemsIndexed(filteredShops) { _, shop ->

                            val isEnabled = shop.isActive

                            val isOpenNow = remember(
                                shop.openTime,
                                shop.closeTime
                            ) {
                                ShopTimeUtils.isShopOpen(
                                    shop.openTime,
                                    shop.closeTime
                                )
                            }
                            Pressable(
                                enabled = isEnabled && isOpenNow,
                                onClick = { onShopClick(shop.id) }
                            )
                            {
                                ShopCard(
                                    shop = shop,
                                    isOpen = isOpenNow,
                                    isEnabled = isEnabled
                                )
                            }
                        }
                    }

                }
            }
        }
    }
}

// --------------------------------------------------
// 🔍 CLEAN SEARCH BAR
// --------------------------------------------------
@Composable
fun FancySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, AlmostBlack.copy(alpha = 0.6f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp)
        ) {

            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                tint = AlmostBlack.copy(alpha = 0.6f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(Modifier.width(12.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {

                if (value.isEmpty()) {
                    Text(
                        "Search shop by name or block",
                        color = MediumGray.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = AlmostBlack,
                        fontSize = 16.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MediumGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// --------------------------------------------------
// ✨ SHOP CARD (NO ANIMATION)
// --------------------------------------------------
@Composable
fun ShopCard(
    shop: Shop,
    isOpen: Boolean,
    isEnabled: Boolean
)
 {

     val showOpenState = isEnabled && isOpen
     val statusText = when {
         !isEnabled -> "SHOP DISABLED"
         !isOpen -> "CLOSED • Opens at ${formatTime(shop.openTime)}"
         else -> "OPEN • Closes at ${formatTime(shop.closeTime)}"
     }

     // Background gradient
     val cardBrush = if (showOpenState) {
         Brush.linearGradient(
             listOf(Color(0xFF9CCC65), Color(0xFF689F38))
         )
     } else {
         Brush.linearGradient(
             listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
         )
     }

     Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(cardBrush)
                .border(
                    width = 1.5.dp,
                    color = if (showOpenState)
                        Color.White.copy(alpha = 0.5f)
                    else
                        Color.White.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // ---------------- LEFT CONTENT ----------------
                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = shop.shopName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (showOpenState) Color.White else Color(0xFF757575)
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Block: ${shop.block}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = if (showOpenState)
                                Color.White.copy(alpha = 0.95f)
                            else
                                Color(0xFF9E9E9E),
                            shadow = if (showOpenState) {
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 3f
                                )
                            } else null
                        )
                    )
                    Spacer(Modifier.height(6.dp))

                    if (showOpenState)
                    Text(
                        text = "Close at ${formatTime(shop.closeTime)}",
                        color = White,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                }

                // ---------------- RIGHT CONTENT ----------------
                if (showOpenState) {

                    // OPEN STATE
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Storefront,
                            contentDescription = "Shop Open",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                } else {

                    // CLOSED STATE
                    Column(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.5f))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "CLOSED",
                            color = Color(0xFF616161),
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "Opens at ${formatTime(shop.openTime)}",
                            color = Color(0xFF757575),
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )

                    }
                }
            }
        }
    }
}

private fun formatTime(time: String): String {
    // expects "HH:mm" e.g. "09:00", "18:30"
    return try {
        val parts = time.split(":")
        val hour24 = parts[0].toInt()
        val minute = parts[1]

        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val amPm = if (hour24 < 12) "AM" else "PM"

        "$hour12:$minute $amPm"
    } catch (e: Exception) {
        time // fallback if backend sends garbage
    }
}



// --------------------------------------------------
// 👆 PRESS ANIMATION
// --------------------------------------------------
@Composable
fun Pressable(
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null // 👈 custom feedback only
            ) {
                onClick()
            }
    ) {
        content()
    }
}



// --------------------------------------------------
// 💀 SKELETON
// --------------------------------------------------
@Composable
fun SkeletonShopCard() {
    val shimmer = rememberShimmerBrush()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            Column {
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(shimmer)
                )
            }

            Box(
                modifier = Modifier
                    .size(70.dp)
                    .align(Alignment.CenterEnd)
                    .clip(RoundedCornerShape(14.dp))
                    .background(shimmer)
            )
        }
    }
}

// --------------------------------------------------
// ✨ SHIMMER BRUSH
// --------------------------------------------------
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")

    val x by transition.animateFloat(
        0f,
        1000f,
        infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing)
        ),
        label = "x"
    )

    return Brush.linearGradient(
        listOf(
            MediumGray.copy(alpha = 0.5f),
            OffWhite.copy(alpha = 0.2f),
            MediumGray.copy(alpha = 0.5f)
        ),
        start = Offset(x - 300f, 0f),
        end = Offset(x, 600f)
    )
}