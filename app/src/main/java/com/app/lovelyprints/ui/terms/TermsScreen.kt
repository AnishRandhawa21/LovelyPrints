import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

@Preview(showBackground = true)
@Composable
fun TermsScreen(
    onAccept: (() -> Unit)? = null
) {
    var isChecked by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // ✅ BACKGROUND IMAGE
        Image(
            painter = painterResource(R.drawable.background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )

        // ✅ FOREGROUND CONTENT
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 360.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 56.dp),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF181818)
                )
            )
            {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        "Terms & Conditions",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    @Composable
                    fun PolicyItem(text: String) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.Top
                        ) {

                            Text(
                                text = "•",
                                color = Color(0xFFFFFFFF),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            Text(
                                text = text,
                                color = Color(0xFFCCCCCC),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

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
                        modifier = Modifier.heightIn(max = 220.dp)
                    ) {
                        items(policies) {
                            PolicyItem(text = it)
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFFFF9500)
                            )
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "I accept Terms & Conditions",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        enabled = isChecked,
                        onClick = { onAccept?.invoke() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9500),
                            disabledContainerColor = Color(0xFF555555)
                        )
                    ) {
                        Text("Accept & Continue")
                    }
                }
            }
        }
    }
}

