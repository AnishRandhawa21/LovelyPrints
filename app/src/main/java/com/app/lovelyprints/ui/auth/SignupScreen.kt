package com.app.lovelyprints.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.R
import com.app.lovelyprints.viewmodel.AuthViewModel
import com.app.lovelyprints.viewmodel.AuthViewModelFactory

@Composable
fun SignupScreen(
    viewModelFactory: AuthViewModelFactory,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignupSuccess()
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1B1B1E)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.lovely_prints),
                        contentDescription = "Logo",
                        Modifier.size(150.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Sign-up",
                        color = Color(0xFFFBFBFB),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            viewModel.clearError()
                        },
                        label = { Text("Name", color = Color(0xFFFBFBFB)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF56E0F),   // darker orange
                            unfocusedBorderColor = Color(0xFF424048), // normal orange
                            focusedLabelColor = Color(0xFFF56E0F),
                            cursorColor = Color(0xFF424048),

                            focusedTextColor = Color(0xFFFBFBFB),
                            unfocusedTextColor = Color(0xFFFBFBFB)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearError()
                        },
                        label = { Text("Email", color = Color(0xFFFBFBFB)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF56E0F),   // darker orange
                            unfocusedBorderColor = Color(0xFF424048), // normal orange
                            focusedLabelColor = Color(0xFFF56E0F),
                            cursorColor = Color(0xFF424048),

                            focusedTextColor = Color(0xFFFBFBFB),
                            unfocusedTextColor = Color(0xFFFBFBFB)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearError()
                        },
                        label = { Text("Password", color = Color(0xFFFBFBFB))},
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFF56E0F),   // darker orange
                            unfocusedBorderColor = Color(0xFF424048), // normal orange
                            focusedLabelColor = Color(0xFFF56E0F),
                            cursorColor = Color(0xFF424048),

                            focusedTextColor = Color(0xFFFBFBFB),
                            unfocusedTextColor = Color(0xFFFBFBFB)
                        )
                    )

                    if (uiState.error != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.signup(name, email, password)
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),

                        // 🎯 Rounded corners for modern look
                        shape = RoundedCornerShape(12.dp),

                        // 🎯 Natural Material elevation
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp,
                            disabledElevation = 0.dp
                        ),

                        // 🎯 Clean color system
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9500),
                            disabledContainerColor = Color(0xFFFEAE46),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),

                        enabled = !uiState.isLoading &&
                                name.isNotBlank() &&
                                email.isNotBlank() &&
                                password.isNotBlank()
                    ) {

                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = "Sign Up",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }


                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFFFBFBFB)
                        )
                    ) {
                        Text("Already have an account? Login")
                    }
                }
            }
        }
    }
}