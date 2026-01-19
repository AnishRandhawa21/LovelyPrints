@file:OptIn(ExperimentalAnimationApi::class)

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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween

// ✅ Import for background
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

import com.app.lovelyprints.di.AppContainer
import com.app.lovelyprints.ui.auth.LoginScreen
import com.app.lovelyprints.ui.auth.SignupScreen
import com.app.lovelyprints.ui.home.HomeScreen
import com.app.lovelyprints.ui.order.CreateOrderScreen
import com.app.lovelyprints.ui.orders.OrdersScreen
import com.app.lovelyprints.ui.profile.ProfileScreen
import com.app.lovelyprints.ui.splash.SplashScreen
import com.app.lovelyprints.viewmodel.*

// ✅ Faster animations to reduce white flash visibility
private val enterFromRight = slideInHorizontally(animationSpec = tween(300)) { it }
private val exitToLeft = slideOutHorizontally(animationSpec = tween(300)) { -it }

private val enterFromLeft = slideInHorizontally(animationSpec = tween(300)) { -it }
private val exitToRight = slideOutHorizontally(animationSpec = tween(300)) { it }


@OptIn(ExperimentalAnimationApi::class)
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
        // ✅ CRITICAL FIX: Add dark background to prevent white flash during animations
        modifier = modifier.background(Color(0xFF151419))
    ) {

        // ------------------------------------------------
        // Splash Screen
        // ------------------------------------------------

        composable(
            route = Routes.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            SplashScreen(
                viewModelFactory = SplashViewModelFactory(appContainer.tokenManager),
                onNavigateToLogin = {
                    navController.navigate(Routes.Login.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(Routes.Home.route) {
                        popUpTo(Routes.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ------------------------------------------------
        // Auth Screens (with slide transitions)
        // ------------------------------------------------

        composable(
            route = Routes.Login.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { -it }
            },
            popEnterTransition = {
                slideInHorizontally(animationSpec = tween(300)) { -it }
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
                slideInHorizontally(animationSpec = tween(300)) { it }
            },
            popExitTransition = {
                slideOutHorizontally(animationSpec = tween(300)) { it }
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

        composable(
            route = Routes.Main.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) }
        ) {
            navController.navigate(Routes.Home.route) {
                popUpTo(Routes.Main.route) { inclusive = true }
            }
        }

        composable(
            route = Routes.Home.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            HomeScreen(
                viewModelFactory = HomeViewModelFactory(appContainer.shopRepository),
                onShopClick = { shopId ->
                    navController.navigate(Routes.CreateOrder.createRoute(shopId))
                }
            )
        }

        composable(
            route = Routes.Orders.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            OrdersScreen(
                viewModelFactory = OrdersViewModelFactory(appContainer.orderRepository)
            )
        }

        composable(
            route = Routes.Profile.route,
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
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
                        popUpTo(Routes.Home.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

            )
        }
    }
}