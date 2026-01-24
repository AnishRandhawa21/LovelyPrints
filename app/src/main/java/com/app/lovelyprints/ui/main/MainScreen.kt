package com.app.lovelyprints.ui.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.lovelyprints.ui.navigation.Routes

// ------------------------------------------------------------
// Model
// ------------------------------------------------------------
data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

// ------------------------------------------------------------
// Fake glass indicator (120Hz safe)
// ------------------------------------------------------------
@Composable
private fun GlassIndicator(
    selected: Boolean,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(64.dp),
        contentAlignment = Alignment.Center
    ) {

        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(50))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                Color.White.copy(alpha = 0.18f)
                            )
                        )
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.4f)
                        ),
                        RoundedCornerShape(50)
                    )
            )
        }

        content()
    }
}

// ------------------------------------------------------------
// Main Screen
// ------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    content: @Composable (Modifier) -> Unit
) {

    val bottomNavItems = listOf(
        BottomNavItem(Routes.Home.route, Icons.Default.Home, "Home"),
        BottomNavItem(Routes.Orders.route, Icons.AutoMirrored.Filled.List, "Orders"),
        BottomNavItem(Routes.Profile.route, Icons.Default.Person, "Profile")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,

        bottomBar = {
            if (currentRoute in bottomNavItems.map { it.route }) {

                NavigationBar(
                    modifier = Modifier.shadow(
                        elevation = 12.dp,
                        shape = RectangleShape,
                        ambientColor = Color.Black.copy(alpha = 0.5f),
                        spotColor = Color.Black.copy(alpha = 0.5f)
                    ),
                    containerColor = Color(0xFF1B1B1E)
                ) {

                    bottomNavItems.forEach { item ->

                        val selected = currentRoute == item.route

                        val itemColor =
                            if (selected) Color(0xFFFF8840)
                            else Color(0xFF878787).copy(alpha = 0.6f)

                        NavigationBarItem(

                            selected = selected,

                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(
                                            navController.graph
                                                .findStartDestination().id
                                        ) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },

                            icon = {
                                GlassIndicator(selected = selected) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = itemColor
                                    )
                                }
                            },

                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    color = itemColor
                                )
                            },

                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = Color.Transparent,
                                selectedIconColor = itemColor,
                                unselectedIconColor = itemColor,
                                selectedTextColor = itemColor,
                                unselectedTextColor = itemColor
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        content(Modifier.padding(paddingValues))
    }
}
