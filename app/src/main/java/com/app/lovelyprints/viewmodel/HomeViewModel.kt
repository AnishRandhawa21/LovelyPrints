package com.app.lovelyprints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lovelyprints.data.model.Shop
import com.app.lovelyprints.data.repository.Result
import com.app.lovelyprints.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val shops: List<Shop> = emptyList(),
    val error: String? = null
)

class HomeViewModel(private val shopRepository: ShopRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadShops()
    }

    fun loadShops() {
        viewModelScope.launch {
            _uiState.value = HomeUiState(isLoading = true)
            when (val result = shopRepository.getShops()) {
                is Result.Success -> {
                    _uiState.value = HomeUiState(shops = result.data)
                }
                is Result.Error -> {
                    _uiState.value = HomeUiState(error = result.message)
                }
                else -> {}
            }
        }
    }
    fun isShopOpen(shop: Shop): Boolean {
        return ShopTimeUtils.isShopOpen(
            shop.openTime,
            shop.closeTime
        )
    }

}