package com.app.lovelyprints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lovelyprints.data.model.Order
import com.app.lovelyprints.data.repository.OrderRepository
import com.app.lovelyprints.data.repository.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OrdersUiState(
    val isLoading: Boolean = false,
    val currentOrders: List<Order> = emptyList(),
    val orderHistory: List<Order> = emptyList(),
    val error: String? = null
)

class OrdersViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {

            _uiState.value = OrdersUiState(isLoading = true)

            when (val result = orderRepository.getOrders()) {

                is Result.Success -> {

                    val allOrders = result.data.data

                    val current =
                        allOrders.filter { it.status.lowercase() != "completed" }

                    val history =
                        allOrders.filter { it.status.lowercase() == "completed" }

                    _uiState.value = OrdersUiState(
                        currentOrders = current,
                        orderHistory = history
                    )
                }

                is Result.Error -> {
                    _uiState.value = OrdersUiState(error = result.message)
                }

                else -> Unit
            }
        }
    }
}
