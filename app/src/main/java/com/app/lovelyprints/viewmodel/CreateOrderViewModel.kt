package com.app.lovelyprints.viewmodel

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

/* ---------------- UI STATE ---------------- */

data class CreateOrderUiState(
    val isLoading: Boolean = false,
    val printOptions: PrintOptions? = null,
    val selectedFile: File? = null,
    val selectedPaperType: PaperType? = null,
    val selectedColorMode: ColorMode? = null,
    val selectedFinishType: FinishType? = null,
    val pageCount: Int = 1,
    val copies: Int = 1,
    val orientation: String = "portrait",
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
                            selectedPaperType = result.data.paperTypes?.firstOrNull(),
                            selectedColorMode = result.data.colorModes?.firstOrNull(),
                            selectedFinishType = result.data.finishTypes?.firstOrNull(),
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

    fun setOrientation(orientation: String) {
        _uiState.value = _uiState.value.copy(orientation = orientation)
    }

    fun setUrgent(isUrgent: Boolean) {
        _uiState.value = _uiState.value.copy(isUrgent = isUrgent)
    }

    /* ---------------- ORDER FLOW ---------------- */

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
            val orderResult = orderRepository.createOrder(
                shopId = shopId,
                description = "Print order",
                orientation = state.orientation,
                isUrgent = state.isUrgent
            )

            if (orderResult !is Result.Success) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = (orderResult as Result.Error).message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            val orderId = orderResult.data.id
            currentOrderId = orderId

            /* 2️⃣ UPLOAD FILE */
            _uiState.value = state.copy(currentStep = OrderStep.UPLOADING)

            val uploadResult = orderRepository.uploadFile(file)
            if (uploadResult !is Result.Success) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = (uploadResult as Result.Error).message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            // 🔥 FIX 3 — FILE KEY SAFETY
            val fileKey = uploadResult.data.fileKey
            if (fileKey.isNullOrBlank()) {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = "File upload failed. Please try again.",
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            /* 3️⃣ ATTACH DOCUMENT */
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
                _uiState.value = state.copy(
                    isLoading = false,
                    error = (attachResult as Result.Error).message,
                    currentStep = OrderStep.SELECT_OPTIONS
                )
                return@launch
            }

            /* 4️⃣ CREATE PAYMENT */
            _uiState.value = state.copy(currentStep = OrderStep.PROCESSING_PAYMENT)

            val paymentResult = orderRepository.createPayment(orderId)

            if (paymentResult is Result.Success) {
                onPaymentRequired(
                    paymentResult.data.id,
                    paymentResult.data.amount
                )
            } else {
                _uiState.value = state.copy(
                    isLoading = false,
                    error = (paymentResult as Result.Error).message,
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
        val orderId = currentOrderId ?: return

        viewModelScope.launch {
            val result = orderRepository.verifyPayment(
                razorpayOrderId,
                razorpayPaymentId,
                razorpaySignature,
                orderId
            )

            if (result is Result.Success) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    currentStep = OrderStep.SUCCESS
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = (result as Result.Error).message
                )
            }
        }
    }
}
