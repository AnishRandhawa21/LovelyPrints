package com.app.lovelyprints.data.repository

import com.app.lovelyprints.data.api.ShopApi
import com.app.lovelyprints.data.model.PrintOptions
import com.app.lovelyprints.data.model.Shop

class ShopRepository(
    private val shopApi: ShopApi
) {

    suspend fun getShops(): Result<List<Shop>> {
        return try {
            val response = shopApi.getShops()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error("Empty response")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPrintOptions(shopId: String): Result<PrintOptions> {
        return try {
            val response = shopApi.getPrintOptions(shopId)
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                Result.Error(response.message() ?: "Failed to fetch options")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }
}
