package com.app.lovelyprints.di

import android.content.Context
import com.app.lovelyprints.core.auth.TokenManager
import com.app.lovelyprints.core.network.ApiClient
import com.app.lovelyprints.data.api.AuthApi
import com.app.lovelyprints.data.api.OrderApi
import com.app.lovelyprints.data.api.ShopApi
import com.app.lovelyprints.data.repository.AuthRepository
import com.app.lovelyprints.data.repository.OrderRepository
import com.app.lovelyprints.data.repository.ShopRepository

class AppContainer(context: Context, onUnauthorized: () -> Unit) {

    val tokenManager = TokenManager(context)

    private val apiClient = ApiClient(tokenManager, onUnauthorized)

    private val authApi: AuthApi = apiClient.createService()
    private val shopApi: ShopApi = apiClient.createService()
    private val orderApi: OrderApi = apiClient.createService()

    val authRepository = AuthRepository(authApi, tokenManager)
    val shopRepository = ShopRepository(shopApi)
    val orderRepository = OrderRepository(orderApi)
}