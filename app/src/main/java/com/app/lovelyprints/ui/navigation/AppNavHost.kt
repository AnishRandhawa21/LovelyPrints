package com.app.lovelyprints.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument

// ✅ Correct animation + navigation imports
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

import com.app.lovelyprints.di.AppContainer
import com.app.lovelyprints.ui.auth.LoginScreen
import com.app.lovelyprints.ui.auth.SignupScreen
import com.app.lovelyprints.ui.home.HomeScreen
import com.app.lovelyprints.ui.order.CreateOrderScreen
import com.app.lovelyprints.ui.orders.OrdersScreen
import com.app.lovelyprints.ui.profile.ProfileScreen
import com.app.lovelyprints.viewmodel.*

@OptIn(ExperimentalAnimationApi::class)   // ✅ correct OptIn
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    appContainer: AppContainer,
    modifier: Modifier = Modifier
) {

    AnimatedNavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // ------------------------------------------------
        // Auth Screens (with slide transitions)
        // ------------------------------------------------

        composable(
            route = Routes.Login.route,
            exitTransition = {
                slideOutHorizontally { -it }
            },
            popEnterTransition = {
                slideInHorizontally { -it }
            }
        ) {
            LoginScreen(
                viewModelFactory = AuthViewModelFactory(
                    appContainer.authRepository,
                    appContainer.tokenManager
                ),
                onLoginSuccess = {
                    navController.navigate(Routes.Main.route) {
                        popUpTo(Routes.Login.route) { inclusive = true }
                    }
                },
                onNavigateToSignup = {
                    navController.navigate(Routes.Signup.route)
                }
            )
        }

        composable(
            route = Routes.Signup.route,
            enterTransition = {
                slideInHorizontally { it }
            },
            popExitTransition = {
                slideOutHorizontally { it }
            }
        ) {
            SignupScreen(
                viewModelFactory = AuthViewModelFactory(
                    appContainer.authRepository,
                    appContainer.tokenManager
                ),
                onSignupSuccess = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Signup.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // ------------------------------------------------
        // Main Flow
        // ------------------------------------------------

        composable(Routes.Main.route) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Main.route) { inclusive = true }
            }
        }

        composable(Routes.Home.route) {
            HomeScreen(
                viewModelFactory = HomeViewModelFactory(appContainer.shopRepository),
                onShopClick = { shopId ->
                    navController.navigate(Routes.CreateOrder.createRoute(shopId))
                }
            )
        }

        composable(Routes.Orders.route) {
            OrdersScreen(
                viewModelFactory = OrdersViewModelFactory(appContainer.orderRepository)
            )
        }

        composable(Routes.Profile.route) {
            ProfileScreen(
                viewModelFactory = ProfileViewModelFactory(appContainer.authRepository),
                tokenManager = appContainer.tokenManager,
                onLogout = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // ------------------------------------------------
        // Create Order (with argument)
        // ------------------------------------------------

        composable(
            route = Routes.CreateOrder.route,
            arguments = listOf(
                navArgument("shopId") { type = NavType.StringType }
            )
        ) { backStackEntry ->

            val shopId = backStackEntry.arguments?.getString("shopId")
                ?: return@composable

            CreateOrderScreen(
                shopId = shopId,
                viewModelFactory = CreateOrderViewModelFactory(
                    appContainer.shopRepository,
                    appContainer.orderRepository,
                    shopId
                ),
                onOrderSuccess = {
                    navController.navigate(Routes.Orders.route) {
                        popUpTo(Routes.Home.route)
                    }
                }
            )
        }
    }
}
