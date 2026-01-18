package com.app.lovelyprints.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.data.model.Shop
import com.app.lovelyprints.theme.Inter
import com.app.lovelyprints.viewmodel.HomeViewModel
import com.app.lovelyprints.viewmodel.HomeViewModelFactory

// --------------------------------------------------
// 🏠 HOME SCREEN
// --------------------------------------------------
@Composable
fun HomeScreen(
    viewModelFactory: HomeViewModelFactory,
    onShopClick: (String) -> Unit
) {
    val viewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    // 🔍 Search state
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF151419),
                        Color(0xFF151419)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --------------------------------------------------
            // TITLE
            // --------------------------------------------------
            Text(
                text = "Available Shops",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = Inter,
                modifier = Modifier.padding(bottom = 10.dp),
                color = Color(0xFF878787)
            )
            // --------------------------------------------------
            // 🔍 FANCY SEARCH BAR
            // --------------------------------------------------
            FancySearchBar(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClearClick = { searchQuery = "" }
            )

            Spacer(modifier = Modifier.height(16.dp))

            when {
                // --------------------------------------------------
                // LOADING
                // --------------------------------------------------
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

                // --------------------------------------------------
                // ERROR
                // --------------------------------------------------
                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error ?: "Something went wrong",
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadShops() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF9500)
                            )) {
                            Text("Retry")
                        }
                    }
                }

                // --------------------------------------------------
                // EMPTY
                // --------------------------------------------------
                uiState.shops.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No shops available")
                    }
                }

                // --------------------------------------------------#878787
                // CONTENT
                // --------------------------------------------------
                else -> {

                    // 🎨 Gradient sets for cards
                    val cardGradients = listOf(
                        listOf(Color(0xFFE88504), Color(0xFFFF9500)),
                        listOf(Color(0xFFFF9500), Color(0xFFE88504)),
                        listOf(Color(0xFFE88504), Color(0xFFFF9500))
                    )

                    // 🔍 Reorder list so matches come first
                    val sortedShops = uiState.shops.sortedWith(
                        compareByDescending<Shop> {
                            it.shopName.contains(searchQuery, ignoreCase = true)
                        }
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(sortedShops) { index, shop ->

                            val gradientColors =
                                cardGradients[index % cardGradients.size]

                            val brush = Brush.linearGradient(
                                colors = gradientColors,
                                start = Offset(500f, 700f),   // bottom
                                end = Offset(200f, 0f)     // top-right
                            )

                            ShopCard(
                                shop = shop,
                                backgroundBrush = brush,
                                onClick = { onShopClick(shop.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// 🔍 FANCY SEARCH BAR (like your image)
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
            .height(64.dp),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(
                    color = Color(0xFF363636),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFFF56E0F),
                    modifier = Modifier.padding(start = 8.dp)
                )

                Spacer(Modifier.width(8.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        color = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                ) { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            "Search shop...",
                            color = Color.Gray
                        )
                    }
                    innerTextField()
                }

                if (value.isNotEmpty()) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// --------------------------------------------------
// 🧩 SHOP CARD
// --------------------------------------------------
@Composable
fun ShopCard(
    shop: Shop,
    backgroundBrush: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(
                brush = backgroundBrush,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // -------- Title with shadow --------
            Text(
                text = shop.shopName,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(1.5f, 1.5f),
                        blurRadius = 6f
                    )
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // -------- Subtitle with softer shadow --------
            Text(
                text = "Block: ${shop.block}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.35f),
                        offset = Offset(1f, 1f),
                        blurRadius = 4f
                    )
                )
            )
        }
    }
}
