package com.app.lovelyprints.ui.terms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.lovelyprints.R
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import com.app.lovelyprints.theme.AlmostBlack
import com.app.lovelyprints.theme.Cream
import com.app.lovelyprints.theme.LimeGreen
import com.app.lovelyprints.theme.MediumGray

@Preview(showBackground = true)
@Composable
fun TermsScreen(
    onAccept: (() -> Unit)? = null
) {
    var isChecked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.kaagazlogo),
                contentDescription = "Logo",
                modifier = Modifier.size(100.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Header text
            Text(
                text = "Terms & Conditions",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = AlmostBlack
            )

            Text(
                text = "Please read and accept to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MediumGray,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Terms card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Policies list
                    val policies = listOf(
                        "Orders once paid are final and cannot be cancelled or refunded",
                        "All uploaded files are automatically scanned for inappropriate or prohibited content; violations may result in immediate account suspension or permanent ban",
                        "Customers must carry the pickup OTP to collect their prints",
                        "Orders must be collected on the same day they are printed; uncollected orders will be discarded after the same day",
                        "Neither the print shop nor Lovely Prints will be responsible for any loss, damage, or claims related to uncollected or discarded orders",
                        "Print shops are not responsible for errors caused by incorrect or low-quality files uploaded by users",
                        "Misuse of the platform, including policy violations or abusive behavior, may lead to account suspension or permanent termination"
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(policies) { policy ->
                            PolicyItem(text = policy)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Divider
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MediumGray.copy(alpha = 0.2f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Checkbox row
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Cream.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { isChecked = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = LimeGreen,
                                    uncheckedColor = MediumGray.copy(alpha = 0.5f),
                                    checkmarkColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "I accept the Terms & Conditions",
                                color = AlmostBlack,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Accept button
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = isChecked,
                        onClick = { onAccept?.invoke() },
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 8.dp,
                            disabledElevation = 0.dp
                        ),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LimeGreen,
                            disabledContainerColor = MediumGray.copy(alpha = 0.4f),
                            contentColor = Color.White,
                            disabledContentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(
                            text = "Accept & Continue",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PolicyItem(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Cream.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = "•",
                color = LimeGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 12.dp, top = 2.dp)
            )

            Text(
                text = text,
                color = AlmostBlack,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}