package com.app.lovelyprints

import com.app.lovelyprints.ui.terms.TermsScreen
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.app.lovelyprints.data.model.RazorpayHolder
import com.app.lovelyprints.data.model.RazorpayResult
import com.app.lovelyprints.theme.LovelyPrintsTheme
import com.app.lovelyprints.ui.main.MainScreen
import com.app.lovelyprints.ui.navigation.AppNavHost
import com.app.lovelyprints.ui.navigation.Routes
import com.app.lovelyprints.theme.Cream
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.google.firebase.messaging.FirebaseMessaging
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat


class MainActivity : ComponentActivity(), PaymentResultWithDataListener {
    
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            Log.d("NOTIFICATION", "Permission granted = $isGranted")
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Checkout.preload(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d("FCM", "Fetching FCM failed")
                    return@addOnCompleteListener
                }

                val token = task.result
                Log.d("FCM", "Manual token fetch → $token")
            }



        setContent {

            LovelyPrintsTheme {

                FixSystemBars(enabled = true)

                // ✅ create ONCE
                val navController = rememberNavController()

                // ✅ UI-only overlay flag
                var showTerms by rememberSaveable {
                    mutableStateOf(true)
                }

                Box {

                    // ✅ NEVER recreated
                    MainScreen(navController) { padding ->
                        AppNavHost(
                            navController = navController,
                            startDestination = Routes.Splash.route,
                            appContainer =
                                (application as LovelyPrintsApp).appContainer,
                            modifier = padding
                        )
                    }

                    // ✅ pure UI overlay
                    if (showTerms) {
                        TermsScreen(
                            onAccept = {
                                showTerms = false
                            }
                        )
                    }
                }
            }
        }
    }


    // --------------------------------------------------
    // Razorpay callbacks
    // --------------------------------------------------

    override fun onPaymentSuccess(
        razorpayPaymentId: String?,
        paymentData: PaymentData?
    ) {
        val orderId = paymentData?.orderId ?: return
        val paymentId = paymentData.paymentId ?: return
        val signature = paymentData.signature ?: return

        RazorpayHolder.result =
            RazorpayResult(orderId, paymentId, signature)

        Log.d("RAZORPAY", "PAYMENT SUCCESS")
    }

    override fun onPaymentError(
        code: Int,
        description: String?,
        paymentData: PaymentData?
    ) {
        // 🔹 user cancelled payment (pressed back / closed)
        if (code == 0) {

            RazorpayHolder.result =
                RazorpayResult(
                    orderId = paymentData?.orderId ?: "",
                    paymentId = "",
                    signature = ""
                )

            Log.d("RAZORPAY", "PAYMENT CANCELLED BY USER")
            return
        }

        // 🔹 real payment failure
        RazorpayHolder.result =
            RazorpayResult(
                orderId = paymentData?.orderId ?: "",
                paymentId = "",
                signature = ""
            )

        Log.e("RAZORPAY", "PAYMENT FAILED → $description")
    }

}

/* -------------------------------------------------- */
/* ---------------- SYSTEM BAR FIX ------------------- */
/* -------------------------------------------------- */

@Composable
fun FixSystemBars(enabled: Boolean) {

    val view = LocalView.current
    val backgroundColor = Cream // Changed to Cream background

    SideEffect {
        if (!enabled) return@SideEffect

        val window = (view.context as Activity).window

        // Set system bars to Cream color
        window.statusBarColor = backgroundColor.toArgb()
        window.navigationBarColor = backgroundColor.toArgb()

        WindowCompat.getInsetsController(window, view).apply {
            // Light icons for light background
            isAppearanceLightStatusBars = true
            isAppearanceLightNavigationBars = true
        }
    }
}

