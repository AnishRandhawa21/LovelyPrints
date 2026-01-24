package com.app.lovelyprints.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lovelyprints.data.model.*
import com.app.lovelyprints.data.repository.OrderRepository
import com.app.lovelyprints.data.repository.Result
import com.app.lovelyprints.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import android.content.Context
import com.app.lovelyprints.utils.PdfUtils
import kotlinx.coroutines.flow.update

/* ---------------- UI STATE ---------------- */

data class CreateOrderUiState(
    val isLoading: Boolean = false,
    val printOptions: PrintOptions? = null,
    val description: String = "",
    val selectedFile: File? = null,
    val selectedPaperType: PaperType? = null,
    val selectedColorMode: ColorMode? = null,
    val selectedFinishType: FinishType? = null,
    val pageCount: Int = 1,
    val copies: Int = 1,
    val orientation: PrintOrientation = PrintOrientation.PORTRAIT,
    val isUrgent: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val currentStep: OrderStep = OrderStep.LOADING_OPTIONS
)

enum class OrderStep {
    LOADING_OPTIONS,
    SELECT_OPTIONS,
    UPLOADING,
    CREATING_ORDER,
    PROCESSING_PAYMENT,
    SUCCESS
}

/* ---------------- VIEWMODEL ---------------- */

class CreateOrderViewModel(
    private val shopRepository: ShopRepository,
    private val orderRepository: OrderRepository,
    private val shopId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateOrderUiState())
    val uiState: StateFlow<CreateOrderUiState> = _uiState.asStateFlow()

    private var currentOrderId: String? = null

    init {
        loadPrintOptions()
    }

    /* ---------------- LOAD OPTIONS ---------------- */

    private fun loadPrintOptions() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                when (val result = shopRepository.getPrintOptions(shopId)) {

                    is Result.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            printOptions = result.data,
                            selectedPaperType = null,
                            selectedColorMode = result.data.colorModes?.firstOrNull(),
                            selectedFinishType = null,
                            currentStep = OrderStep.SELECT_OPTIONS
                        )
                    }

                    is Result.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message,
                            currentStep = OrderStep.SELECT_OPTIONS
                        )
                    }

                    is Result.Loading -> {
                        _uiState.value = _uiState.value.copy(isLoading = true)
                    }
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load print options",
                    currentStep = OrderStep.SELECT_OPTIONS
                )
            }
        }
    }

    /* ---------------- STATE SETTERS ---------------- */

    fun setFile(file: File) {
        _uiState.value = _uiState.value.copy(selectedFile = file)
    }

    fun setPaperType(paperType: PaperType) {
        _uiState.value = _uiState.value.copy(selectedPaperType = paperType)
    }

    fun setColorMode(colorMode: ColorMode) {
        _uiState.value = _uiState.value.copy(selectedColorMode = colorMode)
    }

    fun setFinishType(finishType: FinishType) {
        _uiState.value = _uiState.value.copy(selectedFinishType = finishType)
    }

    fun setPageCount(count: Int) {
        _uiState.value = _uiState.value.copy(pageCount = count)
    }


    fun setCopies(copies: Int) {
        _uiState.value = _uiState.value.copy(copies = copies)
    }

    fun setOrientation(orientation: PrintOrientation) {
        _uiState.value = _uiState.value.copy(orientation = orientation)
    }
    fun setUrgent(isUrgent: Boolean) {
        _uiState.value = _uiState.value.copy(isUrgent = isUrgent)
    }
    fun setDescription(value: String) {
        _uiState.value = _uiState.value.copy(
            description = value
        )
    }
    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }


    /* ---------------- ORDER FLOW ---------------- */

    fun setFileAndReadPages(context: Context, file: File) {
        _uiState.value = _uiState.value.copy(selectedFile = file)

        viewModelScope.launch {
            try {
                val pages = PdfUtils.getPdfPageCount(context, file)
                _uiState.value = _uiState.value.copy(pageCount = pages)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to read PDF pages"
                )
            }
        }
    }

    fun submitOrder(onPaymentRequired: (String, Int) -> Unit) {
        viewModelScope.launch {

            val state = _uiState.value

            /* ---------- FILE CHECK ---------- */
            val file = state.selectedFile ?: run {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Please select a file"
                )
                return@launch
            }

            /* ---------- OPTIONS CHECK ---------- */
            val paper = state.selectedPaperType
            val color = state.selectedColorMode
            val finish = state.selectedFinishType

            if (paper == null || color == null || finish == null) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "Please select print options first"
                )
                return@launch
            }

            _uiState.value = state.copy(
                isLoading = true,
                currentStep = OrderStep.CREATING_ORDER
            )

            /* 1️⃣ CREATE ORDER */
            Log.d("CREATE_ORDER_VM", "Creating order for shop: $shopId")

            val orderResult = orderRepository.createOrder(
                shopId = shopId,
                description = state.description.ifBlank { "Print order" },
                orientation = state.orientation,
                isUrgent = state.isUrgent
            )

            if (orderResult !is Result.Success) {
                Log.e(
                    "CREATE_ORDER_VM",
                    "Order creation failed: ${(orderResult as Result.Error).message}"
                )
                _uiState.value = state.copy(
                    isLoading = false,
                    error = orderResult.message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            val orderId = orderResult.data.id
            currentOrderId = orderId
            Log.d("CREATE_ORDER_VM", "✅ Order created successfully with ID: $orderId")

            /* 2️⃣ UPLOAD FILE */
            _uiState.value = state.copy(currentStep = OrderStep.UPLOADING)
            Log.d("CREATE_ORDER_VM", "Uploading file: ${file.name}")

            val uploadResult = orderRepository.uploadFile(file)
            if (uploadResult !is Result.Success) {
                Log.e(
                    "CREATE_ORDER_VM",
                    "File upload failed: ${(uploadResult as Result.Error).message}"
                )
                _uiState.value = state.copy(
                    isLoading = false,
                    error = uploadResult.message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            val fileKey = uploadResult.data.fileKey
            if (fileKey.isNullOrBlank()) {
                Log.e("CREATE_ORDER_VM", "File key is null or blank")
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "File upload failed. Please try again.",
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            Log.d("CREATE_ORDER_VM", "✅ File uploaded successfully with key: $fileKey")

            /* 3️⃣ ATTACH DOCUMENT */
            Log.d("CREATE_ORDER_VM", "Attaching document to order: $orderId")

            val attachResult = orderRepository.attachDocument(
                orderId = orderId,
                fileKey = fileKey,
                fileName = file.name,
                pageCount = state.pageCount,
                copies = state.copies,
                paperTypeId = paper.id,
                colorModeId = color.id,
                finishTypeId = finish.id
            )

            if (attachResult !is Result.Success) {
                Log.e(
                    "CREATE_ORDER_VM",
                    "Document attachment failed: ${(attachResult as Result.Error).message}"
                )
                _uiState.value = state.copy(
                    isLoading = false,
                    error = attachResult.message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            Log.d("CREATE_ORDER_VM", "✅ Document attached successfully")

// 🔥 ADD THIS LINE - Wait for backend to calculate total_price
            kotlinx.coroutines.delay(1500)

            /* 4️⃣ CREATE PAYMENT */
            _uiState.value = state.copy(currentStep = OrderStep.PROCESSING_PAYMENT)

// Wait for backend to calculate total_price
            kotlinx.coroutines.delay(1500)

            Log.d("CREATE_ORDER_VM", "Creating payment for order ID: $orderId")

            val paymentResult = orderRepository.createPayment(orderId)

            if (paymentResult is Result.Success) {
                val razorpayOrderId = paymentResult.data.id
                val amount = paymentResult.data.amount

                if (razorpayOrderId.isNullOrBlank() || amount == null || amount == 0) {
                    Log.e("CREATE_ORDER_VM", "Invalid payment data: id=$razorpayOrderId, amount=$amount")
                    _uiState.value = state.copy(
                        isLoading = false,
                        error = "Payment creation failed",
                        currentStep = OrderStep.SELECT_OPTIONS
                    )
                    return@launch
                }

                Log.d("CREATE_ORDER_VM", "✅ Payment valid: razorpay_order_id=$razorpayOrderId, amount=$amount")
                onPaymentRequired(razorpayOrderId, amount)
            } else {
                Log.e("CREATE_ORDER_VM", "Payment creation failed: ${(paymentResult as Result.Error).message}")
                _uiState.value = state.copy(
                    isLoading = false,
                    error = paymentResult.message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
            }
        }
    }

    /* ---------------- VERIFY PAYMENT ---------------- */

    fun verifyPayment(
        razorpayOrderId: String,
        razorpayPaymentId: String,
        razorpaySignature: String
    ) {
        val orderId = currentOrderId ?: run {
            Log.e("CREATE_ORDER_VM", "Cannot verify payment: currentOrderId is null")
            return
        }

        Log.d("CREATE_ORDER_VM", "Verifying payment for order: $orderId")

        viewModelScope.launch {
            val result = orderRepository.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                orderId
            )

            if (result is Result.Success) {
                Log.d("CREATE_ORDER_VM", "✅ Payment verified successfully")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentStep = OrderStep.SUCCESS
                )
            } else {
                Log.e(
                    "CREATE_ORDER_VM",
                    "Payment verification failed: ${(result as Result.Error).message}"
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }
}
