package com.app.lovelyprints.ui.order

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown

import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.lovelyprints.data.model.*
import com.app.lovelyprints.viewmodel.*
import com.razorpay.Checkout
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/* -------------------------------------------------- */
/* ---------------- MAIN SCREEN ----------------------*/
/* -------------------------------------------------- */

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(
                color = color,
                shape = RoundedCornerShape(9.dp)
            )
    )
}

@Composable
fun CreateOrderScreen(
    shopId: String,
    viewModelFactory: CreateOrderViewModelFactory,
    onOrderSuccess: () -> Unit
) {
    val viewModel: CreateOrderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    val activity = remember(context) {
        var ctx: Context = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    val filePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri ?: return@rememberLauncherForActivityResult

            val fileName = getFileName(context, uri)
            val input = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, fileName)

            input?.use { inp ->
                FileOutputStream(file).use { out ->
                    inp.copyTo(out)
                }
            }

            viewModel.setFileAndReadPages(context, file)
        }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onOrderSuccess()
    }

    when (uiState.currentStep) {

        OrderStep.LOADING_OPTIONS ->
            LoadingScreen("Loading print options...")

        OrderStep.SELECT_OPTIONS ->
            SelectOptionsContent(
                uiState = uiState,
                onFileSelect = { filePickerLauncher.launch("application/pdf") },
                onPaperTypeSelect = viewModel::setPaperType,
                onColorModeSelect = viewModel::setColorMode,
                onFinishTypeSelect = viewModel::setFinishType,
                onCopiesChange = viewModel::setCopies,
                onOrientationChange = viewModel::setOrientation,
                onUrgentChange = viewModel::setUrgent,
                onSubmit = {
                    if (activity == null) return@SelectOptionsContent

                    viewModel.submitOrder { razorpayOrderId, amount ->
                        startRazorpayPayment(
                            activity = activity,
                            razorpayOrderId = razorpayOrderId,
                            amount = amount,
                            onSuccess = { paymentId, signature ->
                                viewModel.verifyPayment(
                                    razorpayOrderId,
                                    paymentId,
                                    signature
                                )
                            },
                            onError = {}
                        )
                    }
                }
            )

        OrderStep.UPLOADING ->
            LoadingScreen("Uploading document...")

        OrderStep.CREATING_ORDER ->
            LoadingScreen("Creating order...")

        OrderStep.PROCESSING_PAYMENT ->
            LoadingScreen("Processing payment...")

        OrderStep.SUCCESS ->
            SuccessScreen(onOrderSuccess)
    }

    uiState.error?.let {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClick = onOrderSuccess) { Text("OK") }
            },
            title = { Text("Error") },
            text = { Text(it) }
        )
    }
    LaunchedEffect(Unit) {
        while (true) {

            RazorpayHolder.result?.let {

                viewModel.verifyPayment(
                    razorpayOrderId = it.orderId,
                    razorpayPaymentId = it.paymentId,
                    razorpaySignature = it.signature
                )

                RazorpayHolder.result = null
            }

            kotlinx.coroutines.delay(500)
        }
    }

}

/* -------------------------------------------------- */
/* ---------------- SELECT OPTIONS -------------------*/
/* -------------------------------------------------- */

@Composable
fun SelectOptionsContent(
    uiState: CreateOrderUiState,
    onFileSelect: () -> Unit,
    onPaperTypeSelect: (PaperType) -> Unit,
    onColorModeSelect: (ColorMode) -> Unit,
    onFinishTypeSelect: (FinishType) -> Unit,
    onCopiesChange: (Int) -> Unit,
    onOrientationChange: (String) -> Unit,
    onUrgentChange: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF151419)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = "Create Print Order",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF878787),
                modifier = Modifier.padding(16.dp)
            )

            // File Upload Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF363636),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // File preview area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(Color(0xFF696969), RoundedCornerShape(12.dp))
                            .clickable { onFileSelect() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.selectedFile != null) {
                            FilePreview(
                                file = uiState.selectedFile,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFFFF9500),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Accepted formats: PDF, DOCX, JPG, PNG. Max 100MB per file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCACACA),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Options Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF363636),
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Number of Copies
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Number of Copies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable {
                                        if (uiState.copies > 1) {
                                            onCopiesChange(uiState.copies - 1)
                                        }
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF9500)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "−",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = uiState.copies.toString(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.width(40.dp),
                                textAlign = TextAlign.Center
                            )

                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onCopiesChange(uiState.copies + 1) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFF9500)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+",
                                        color = Color.White,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Color Mode
                    Text(
                        text = "Color Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.printOptions?.colorModes?.forEach { colorMode ->
                            val isSelected = uiState.selectedColorMode == colorMode

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(140.dp)
                                    .clickable { onColorModeSelect(colorMode) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFFFE3CF) else Color(0xFF696969),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 2.dp,
                                    color = if (isSelected) Color(0xFFFF9500) else Color(0xFF838383)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top check icon
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color(0xFFFF9500),
                                                modifier = Modifier.size(22.dp)
                                            )
                                        } else {
                                            Spacer(modifier = Modifier.size(22.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Title section
                                    Column(
                                        modifier = Modifier.height(44.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = colorMode.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else Color.White,
                                            textAlign = TextAlign.Center,
                                            maxLines = 1
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))

                                        Text(
                                            text = "₹${colorMode.extraPrice}/page",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isSelected) Color(0xFF1C1C1C) else Color(0xFFE9E9E9)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Color preview
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (colorMode.name.contains("Color", ignoreCase = true)) {
                                            ColorDot(Color.Red)
                                            ColorDot(Color.Blue)
                                            ColorDot(Color.Green)
                                        } else {
                                            ColorDot(Color.Black)
                                            Box(
                                                modifier = Modifier
                                                    .size(18.dp)
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(Color.White)
                                                    .border(
                                                        width = 1.5.dp,
                                                        color = Color.Black,
                                                        shape = RoundedCornerShape(9.dp)
                                                    )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Paper Type Dropdown
                    DropdownSection(
                        label = "Paper Type",
                        selected = uiState.selectedPaperType?.name ?: "",
                        items = uiState.printOptions?.paperTypes ?: emptyList(),
                        itemText = { "${it.name} - ₹${it.basePrice}" },
                        onSelect = onPaperTypeSelect
                    )
                    Spacer(Modifier.height(24.dp))

                    // Finish Type Dropdown
                    DropdownSection(
                        label = "Finish Type",
                        selected = uiState.selectedFinishType?.name ?: "",
                        items = uiState.printOptions?.finishTypes ?: emptyList(),
                        itemText = { "${it.name} - ₹${it.extraPrice}" },
                        onSelect = onFinishTypeSelect
                    )

                    Spacer(Modifier.height(24.dp))

                    // Orientation
                    Text(
                        text = "Orientation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val orientations = listOf("Portrait", "Landscape")

                        orientations.forEach { orientation ->
                            val isSelected = uiState.orientation == orientation

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 80.dp)
                                    .clickable { onOrientationChange(orientation) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFFFE3CF) else Color(0xFF696969),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 2.dp,
                                    color = if (isSelected) Color(0xFFFF9500) else Color(0xFF838383)
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = orientation,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.Black else Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(100.dp))
        }

        // Bottom Bar
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B1B1E),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total ${uiState.pageCount} pages",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF56E0F)
                        )
                        Text(
                            text = "₹${calculateTotal(uiState)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = onSubmit,
                        enabled = uiState.selectedFile != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF9500),
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .width(180.dp)
                    ) {
                        Text(
                            text = "Proceed To Pay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------- */
/* ---------------- DROPDOWN ------------------------- */
/* -------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSection(
    label: String,
    selected: String,
    items: List<T>,
    itemText: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isSelected = selected.isNotEmpty()

    Column {
        // section title (same as Color Mode heading)
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Surface(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable { expanded = true },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) Color(0xFFFFE3CF) else Color(0xFF696969),
                border = BorderStroke(
                    width = 2.dp,
                    color = if (isSelected) Color(0xFFFF9500) else Color(0xFF838383)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (selected.isEmpty()) "Select $label" else selected,
                        color = if (isSelected) Color.Black else Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        tint = if (isSelected) Color.Black else Color.White
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF2C2C2C))
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = itemText(item),
                                color = Color.White
                            )
                        },
                        onClick = {
                            onSelect(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
/* -------------------------------------------------- */
/* ---------------- HELPERS -------------------------- */
/* -------------------------------------------------- */

private fun calculateTotal(uiState: CreateOrderUiState): Int {
    val paperPrice = uiState.selectedPaperType?.basePrice ?: 0
    val colorPrice = uiState.selectedColorMode?.extraPrice ?: 0
    val finishPrice = uiState.selectedFinishType?.extraPrice ?: 0
    val urgentPrice = if (uiState.isUrgent) 10 else 0

    return (paperPrice + colorPrice + finishPrice) * uiState.pageCount * uiState.copies + urgentPrice
}

@Composable
private fun LoadingScreen(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF151419)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color(0xFFFF9500),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = text,
                color = Color(0xFFB5B5B5),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun SuccessScreen(onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Order placed successfully!", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            Button(onClick = onClick) { Text("View Orders") }
        }
    }
}

/* -------------------------------------------------- */
/* ---------------- RAZORPAY ------------------------- */
/* -------------------------------------------------- */

fun startRazorpayPayment(
    activity: Activity,
    razorpayOrderId: String,
    amount: Int,
    onSuccess: (String, String) -> Unit,
    onError: (String) -> Unit
) {
    val checkout = Checkout()

    // ✅ ONLY THE KEY
    checkout.setKeyID("Api here")

    val options = JSONObject().apply {
        put("order_id", razorpayOrderId)
        put("amount", amount) // ⚠️ no ×100 unless backend sends rupees
        put("currency", "INR")
        put("name", "Lovely Prints")
    }

    try {
        checkout.open(activity, options)
    } catch (e: Exception) {
        onError(e.message ?: "Payment failed")
    }
}


/* -------------------------------------------------- */
/* ---------------- FILE NAME HELPER ----------------- */
/* -------------------------------------------------- */

private fun getFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (it.moveToFirst()) {
            return it.getString(nameIndex)
        }
    }
    return "document.pdf"
}