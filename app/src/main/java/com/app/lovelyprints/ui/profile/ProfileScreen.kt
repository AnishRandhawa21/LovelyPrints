package com.app.lovelyprints.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.core.auth.TokenManager
import com.app.lovelyprints.viewmodel.ProfileViewModel
import com.app.lovelyprints.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModelFactory: ProfileViewModelFactory,
    tokenManager: TokenManager,
    onLogout: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val userName by tokenManager.userNameFlow.collectAsState(initial = "User")
    val scope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },

            // 🔲 popup background
            containerColor = Color(0xFF2B2B2B),

            // 📝 text colors
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCCCCCC),

            title = {
                Text(
                    text = "Logout",
                    color = Color(0xFFFFFFFF),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(
                    text = "Are you sure you want to logout?",
                    color = Color(0xFF878787)
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout(onLogout)
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFFF9500)
                    )
                ) {
                    Text("Logout")
                }
            },

            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }

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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Surface(
                modifier = Modifier.size(100.dp),
                shape = MaterialTheme.shapes.large,
                color = Color(0xFFFFB88F)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = userName?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color(color = 0xFFFF8840)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = userName ?: "User",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(color = 0xFFFFFFFF)

            )

            Spacer(modifier = Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF363636))

            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Account Settings",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color(color = 0xFFFFFFFF)
                    )

                    Divider(
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color(color = 0xFF878787)

                    )

                    TextButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFF9500),        // normal
                            disabledContentColor = Color(0xFF878787) // gray
                        )
                    ) {
                        Text(
                            text = "Logout",
                            color = Color(0xFFFF9500),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF878787)
            )
        }
    }
}