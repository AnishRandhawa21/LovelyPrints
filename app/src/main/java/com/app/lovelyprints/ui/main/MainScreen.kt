package com.app.lovelyprints.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.app.lovelyprints.ui.navigation.Routes

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

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
                    modifier = Modifier
                        .shadow(
                            elevation = 12.dp,
                            shape = RectangleShape,
                            ambientColor = Color.Black.copy(alpha = 0.5f),
                            spotColor = Color.Black.copy(alpha = 0.5f)
                        ),
                    containerColor = Color(0xFF1B1B1E)
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        val scale by animateFloatAsState(
                            targetValue = if (selected) 1.2f else 1f,
                            label = "icon-scale"
                        )
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.scale(scale)
                                )
                                   },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                // 🔥 Selected = Orange highlight
                                selectedIconColor = Color(0xFFFF8840),
                                selectedTextColor = Color(0xFFFF8840),

                                // 💡 Indicator bubble = soft blue
                                indicatorColor = Color(0xFFEBEBEB),

                                // 🌊 Unselected = calm dark blue
                                unselectedIconColor = Color(0xFF878787).copy(alpha = 0.6f),
                                unselectedTextColor = Color(0xFF878787).copy(alpha = 0.6f)
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