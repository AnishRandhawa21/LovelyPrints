package com.app.lovelyprints.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import com.app.lovelyprints.utils.isValidPassword
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import com.app.lovelyprints.data.model.Organisation


@Composable
fun SignupScreen(
    viewModelFactory: AuthViewModelFactory,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordTouched by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val isPasswordValid = isValidPassword(password)


    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {

            Toast.makeText(
                context,
                "Verify your account via email before logging in",
                Toast.LENGTH_LONG
            ).show()

            onSignupSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .imePadding()
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

                    OrganisationDropdown(
                        organisations = uiState.organisations,
                        selectedOrganisation = uiState.selectedOrganisation,
                        onSelect = { viewModel.selectOrganisation(it) },
                        isLoading = uiState.isLoadingOrganisations
                    )

                    Spacer(modifier = Modifier.height(16.dp))


                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            passwordTouched = true
                        },
                        label = { Text("Password", color = Color(0xFFFBFBFB)) },

                        isError = passwordTouched && !isValidPassword(password),

                        supportingText = {
                            if (passwordTouched && !isValidPassword(password)) {
                                Text("Password must be at least 8 characters")
                            }
                        },

                        visualTransformation =
                            if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),

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
                            unfocusedTextColor = Color(0xFFFBFBFB),

                            // 🔥 THIS IS THE FIX
                            errorTextColor = Color(0xFFFBFBFB),
                            errorCursorColor = Color(0xFFFBFBFB)
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
                            viewModel.signup(
                                name,
                                email,
                                password,
                                uiState.selectedOrganisation!!.id
                            )
                        },

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),

                        shape = RoundedCornerShape(12.dp),

                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 10.dp,
                            disabledElevation = 0.dp
                        ),

                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9500),
                            disabledContainerColor = Color(0xFFFEAE46),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        ),

                        enabled =
                            !uiState.isLoading &&
                                    name.isNotBlank() &&
                                    email.isNotBlank() &&
                                    isPasswordValid &&
                                    uiState.selectedOrganisation != null
                    ){

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganisationDropdown(
    organisations: List<Organisation>,
    selectedOrganisation: Organisation?,
    onSelect: (Organisation) -> Unit,
    isLoading: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (!isLoading && organisations.isNotEmpty()) {
                expanded = !expanded
            }
        }
    ) {

        OutlinedTextField(
            value = selectedOrganisation?.name
                ?: if (!isLoading && organisations.isEmpty()) "No organisations available"
                else "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Organisation", color = Color(0xFFFBFBFB)) },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color(0xFFFBFBFB)
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF56E0F),
                unfocusedBorderColor = Color(0xFF424048),
                focusedLabelColor = Color(0xFFF56E0F),
                cursorColor = Color(0xFF424048),
                focusedTextColor = Color(0xFFFBFBFB),
                unfocusedTextColor = Color(0xFFFBFBFB)
            )
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            organisations.forEach { organisation ->
                DropdownMenuItem(
                    text = { Text(organisation.name) },
                    onClick = {
                        onSelect(organisation)
                        expanded = false
                    }
                )
            }
        }
    }
}
