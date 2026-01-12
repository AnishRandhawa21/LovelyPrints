package com.app.lovelyprints.ui.navigation

sealed class Routes(val route: String) {
    object Login : Routes("login")
    object Signup : Routes("signup")
    object Main : Routes("main")
    object Home : Routes("home")
    object Orders : Routes("orders")
    object Profile : Routes("profile")
    object CreateOrder : Routes("create_order/{shopId}") {
        fun createRoute(shopId: String) = "create_order/$shopId"
    }
}