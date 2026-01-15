package com.app.lovelyprints.ui.order

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
fun CreateOrderScreen(
    shopId: String,
    viewModelFactory: CreateOrderViewModelFactory,
    onOrderSuccess: () -> Unit
) {
    val viewModel: CreateOrderViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    // Safely extract Activity
    val activity = remember(context) {
        var ctx: Context = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return@remember ctx
            ctx = ctx.baseContext
        }
        null
    }

    /* ---------------- FILE PICKER ---------------- */

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

            // ✅ ONE call — ViewModel handles file + page count
            viewModel.setFileAndReadPages(context, file)
        }

    /* ---------------- NAVIGATION ---------------- */

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) onOrderSuccess()
    }

    /* ---------------- UI STATE ---------------- */

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

    /* ---------------- ERROR ---------------- */

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
}

/* -------------------------------------------------- */
/* ---------------- SELECT OPTIONS -------------------*/
/* -------------------------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text("Create Print Order", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        Button(onClick = onFileSelect, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.selectedFile?.name ?: "Select PDF")

                if (uiState.pageCount > 0) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Pages detected: ${uiState.pageCount}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        DropdownSection(
            label = "Paper Type",
            selected = uiState.selectedPaperType?.name ?: "",
            items = uiState.printOptions?.paperTypes ?: emptyList(),
            itemText = { "${it.name} - ₹${it.basePrice}" },
            onSelect = onPaperTypeSelect
        )

        DropdownSection(
            label = "Color Mode",
            selected = uiState.selectedColorMode?.name ?: "",
            items = uiState.printOptions?.colorModes ?: emptyList(),
            itemText = { "${it.name} - ₹${it.extraPrice}" },
            onSelect = onColorModeSelect
        )

        DropdownSection(
            label = "Finish Type",
            selected = uiState.selectedFinishType?.name ?: "",
            items = uiState.printOptions?.finishTypes ?: emptyList(),
            itemText = { "${it.name} - ₹${it.extraPrice}" },
            onSelect = onFinishTypeSelect
        )

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            // 🔒 Pages locked (auto-detected)
            OutlinedTextField(
                value = uiState.pageCount.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Pages") },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = uiState.copies.toString(),
                onValueChange = { it.toIntOrNull()?.let(onCopiesChange) },
                label = { Text("Copies") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        )
        {
            Text("Urgent Order \n ₹10")
            Switch(uiState.isUrgent, onUrgentChange)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.selectedFile != null
        ) {
            Text("Place Order")
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

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(itemText(item)) },
                    onClick = {
                        onSelect(item)
                        expanded = false
                    }
                )
            }
        }
    }

    Spacer(Modifier.height(16.dp))
}

/* -------------------------------------------------- */
/* ---------------- UI HELPERS ----------------------- */
/* -------------------------------------------------- */

@Composable
private fun LoadingScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
            Text(text)
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
    checkout.setKeyID("YOUR_RAZORPAY_KEY")

    try {
        val options = JSONObject().apply {
            put("order_id", razorpayOrderId)
            put("amount", amount * 100)
            put("currency", "INR")
            put("name", "Lovely Prints")
        }
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
