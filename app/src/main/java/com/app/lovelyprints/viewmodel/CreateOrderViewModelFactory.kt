package com.app.lovelyprints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.lovelyprints.data.repository.OrderRepository
import com.app.lovelyprints.data.repository.ShopRepository

class CreateOrderViewModelFactory(
    private val shopRepository: ShopRepository,
    private val orderRepository: OrderRepository,
    private val shopId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateOrderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateOrderViewModel(shopRepository, orderRepository, shopId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}