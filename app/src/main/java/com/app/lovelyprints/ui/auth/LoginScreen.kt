package com.app.lovelyprints.ui.auth

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation
@Composable
fun LoginScreen(
    viewModelFactory: AuthViewModelFactory,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Log.d("AUTH", "Error: ${uiState.error}")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()

        )



        // 🔹 Content layer (respects status bar)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )
        {
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
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 32.dp),
            color = Color(0xFFFBFBFB)
        )

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
                        focusedBorderColor = Color(0xFFF56E0F),
                        unfocusedBorderColor = Color(0xFF424048),
                        focusedLabelColor = Color(0xFFF56E0F),
                        cursorColor = Color(0xFF424048),

                        // 🔥 THIS is what you need
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
                    label = { Text("Password", color = Color(0xFFFBFBFB)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,

                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password
                    ),

                    trailingIcon = {

                        val icon =
                            if (passwordVisible)
                                Icons.Default.Visibility
                            else
                                Icons.Default.VisibilityOff

                        IconButton(
                            onClick = { passwordVisible = !passwordVisible }
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color(0xFFFBFBFB)
                            )
                        }
                    },

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFF56E0F),
                        unfocusedBorderColor = Color(0xFF424048),
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
                        Log.d("AUTH", "LOGIN BUTTON CLICKED")
                        viewModel.login(email, password)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),

                    // 🎯 Rounded, modern shape
                    shape = RoundedCornerShape(12.dp),

                    // 🎯 Natural Material elevation (better than colored shadow)
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 10.dp,
                        disabledElevation = 0.dp
                    ),

                    // 🎯 Clean color setup
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF9500),
                        disabledContainerColor = Color(0xFFFEAE46),
                        contentColor = Color.White,
                        disabledContentColor = Color.White.copy(alpha = 0.9f)
                    ),

                    enabled = !uiState.isLoading &&
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
                            text = "Login",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }


                Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onNavigateToSignup,
            colors = ButtonDefaults.textButtonColors(
                contentColor = Color(0xFFFBFBFB)
            )
        ) {
            Text("Don't have an account? Sign up")
        }
    }

    }
    }
    }
}