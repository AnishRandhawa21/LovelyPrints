import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .heightIn(max = 220.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = """
• Orders once placed cannot be cancelled
• Pickup OTP must be shown at shop
• Refunds depend on shop policy 
• Files are deleted after printing
• Prices depend on page count & copies
""".trimIndent(),
                            color = Color(0xFFCCCCCC),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
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

