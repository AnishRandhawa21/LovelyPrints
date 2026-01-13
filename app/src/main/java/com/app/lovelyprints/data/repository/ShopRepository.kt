package com.app.lovelyprints.data.repository

import android.util.Log
import com.app.lovelyprints.data.api.ShopApi
import com.app.lovelyprints.data.model.PrintOptions
import com.app.lovelyprints.data.model.Shop
import com.app.lovelyprints.data.model.ShopListResponse

class ShopRepository(
    private val shopApi: ShopApi
) {

    suspend fun getShops(): Result<List<Shop>> {
        return try {
            val response = shopApi.getShops()

            Log.d("SHOP", "CODE = ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!.data)
            } else {
                Result.Error(
                    response.errorBody()?.string() ?: "Failed to load shops"
                )
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

    suspend fun getPrintOptions(shopId: String): Result<PrintOptions> {
        return try {
            val response = shopApi.getPrintOptions(shopId)

            if (response.isSuccessful && response.body() != null) {

                // ⚠️ because backend wraps in { success, message, data }
                val body = response.body()!!

                Result.Success(body.data)   // <-- THIS LINE MATTERS
            } else {
                Result.Error("Failed to load print options")
            }

        } catch (e: Exception) {
            Result.Error(e.message ?: "Network error")
        }
    }

}
