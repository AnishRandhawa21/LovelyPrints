package com.app.lovelyprints.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.unit.sp


// --------------------------------------------------
// 🏠 HOME SCREEN
// --------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModelFactory: HomeViewModelFactory,
    onShopClick: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()


    var searchQuery by remember { mutableStateOf("") }

    // ✅ SEARCH FILTER (FIX)
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

        // ✅ active shops first, closed last
        result.sortedBy { !it.isActive }
    }
    PullToRefreshBox(
        isRefreshing = uiState.isLoading,
        onRefresh = { viewModel.loadShops() },
        state = pullToRefreshState,
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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151419))
            .padding(16.dp)
    ) {

        Text(
            text = "Available Shops",
            style = MaterialTheme.typography.headlineLarge,
            fontFamily = Inter,
            color = Color(0xFF878787)
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

            filteredShops.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No matching shops",
                        color = Color.Gray
                    )
                }
            }

            else -> {

                val gradients = listOf(
                    listOf(Color(0xFFFF952F), Color(0xFFFFAF50)),
                    listOf(Color(0xFFFFAF50), Color(0xFFFF952F)),
                    listOf(Color(0xFFFF952F), Color(0xFFFFAF50))
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredShops) { index, shop ->

                        Pressable(
                            enabled = shop.isActive,
                            onClick = { onShopClick(shop.id) }
                        )
                        {
                            ShopCard(
                                shop = shop,
                                gradientColors = gradients[index % gradients.size]
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
// 🔍 SEARCH BAR
// --------------------------------------------------
@Composable
fun FancySearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFF363636), RoundedCornerShape(30.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {

            Icon(Icons.Default.Search, null, tint = Color(0xFFF56E0F))

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
            ) {

                if (value.isEmpty()) {
                    Text(
                        "Search shop",
                        color = Color.Gray
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (value.isNotEmpty()) {
                IconButton(onClick = onClearClick) {
                    Icon(Icons.Default.Close, null, tint = Color.Gray)
                }
            }
        }
    }
}

// --------------------------------------------------
// ✨ SHOP CARD
// --------------------------------------------------
@Composable
fun ShopCard(
    shop: Shop,
    gradientColors: List<Color>
) {
    val isActive = shop.isActive
    val animatedBrush =
        if (isActive)
            rememberAnimatedGradient(gradientColors)
        else
            Brush.linearGradient(
                listOf(Color(0xFF3A3A3A), Color(0xFF1E1E1E))
            )


    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .alpha(if (isActive) 1f else 0.45f),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {


        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(animatedBrush)
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(20.dp)
                )
        ) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(Modifier.weight(1f)) {

                    Text(
                        text = shop.shopName,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isActive) Color.White else Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "Block: ${shop.block}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.95f),
                            shadow = Shadow(
                                color = Color(0xFFF57C00).copy(alpha = 0.6f),
                                offset = Offset(1.5f, 1.5f),
                                blurRadius = 4f
                            )
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (shop.isActive)
                                Color.Transparent
                            else
                                Color.Black.copy(alpha = 0.35f)
                        ),
                    contentAlignment = Alignment.Center
                ) {

                    if (shop.isActive) {

                        Image(
                            painter = painterResource(R.drawable.shop),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )

                    } else {

                        Text(
                            text = "CLOSED",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}


// --------------------------------------------------
// 🎨 ANIMATED GRADIENT
// --------------------------------------------------
@Composable
fun rememberAnimatedGradient(colors: List<Color>): Brush {
    val transition = rememberInfiniteTransition(label = "gradient")

    val shift by transition.animateFloat(
        0f,
        600f,
        infiniteRepeatable(
            animation = tween(14000, easing = LinearEasing)
        ),
        label = "shift"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(shift, shift),
        end = Offset(shift + 600f, shift + 600f)
    )
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

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = if (enabled) 1f else 1f
                scaleY = if (enabled) 1f else 1f
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null
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
            .height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
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
            Color(0xFF2A2A2A),
            Color(0xFF3A3A3A),
            Color(0xFF2A2A2A)
        ),
        start = Offset(x - 300f, 0f),
        end = Offset(x, 600f)
    )
}

//Close Badge
@Composable
fun ClosedBlurBadge(modifier: Modifier) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.Black.copy(alpha = 0.6f),
                        Color.Black.copy(alpha = 0.35f)
                    )
                )
            )
            .border(
                1.dp,
                Color.White.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "CLOSED",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

