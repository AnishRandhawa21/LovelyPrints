package com.app.lovelyprints.data.repository

import android.util.Log
import com.app.lovelyprints.data.api.ShopApi
import com.app.lovelyprints.data.model.PrintOptions
import com.app.lovelyprints.data.model.Shop
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ShopRepository(
    private val shopApi: ShopApi
) {

    /* ---------------- GET SHOPS ---------------- */

    suspend fun getShops(): Result<List<Shop>> {
        return try {
            val response = shopApi.getShops()

            Log.d("SHOP", "CODE = ${response.code()}")

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Failed to load shops.")
                }
            }

            val body = response.body()
                ?: return Result.Error("Empty server response")

            Result.Success(body.data)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- PRINT OPTIONS ---------------- */

    suspend fun getPrintOptions(shopId: String): Result<PrintOptions> {
        return try {
            val response = shopApi.getPrintOptions(shopId)

            if (!response.isSuccessful) {
                return when (response.code()) {
                    401, 403 ->
                        Result.Error("Session expired. Please login again.")

                    404 ->
                        Result.Error("Print options not found.")

                    500 ->
                        Result.Error("Server error. Please try again later.")

                    else ->
                        Result.Error("Failed to load print options.")
                }
            }

            // backend wrapper: { success, message, data }
            val body = response.body()
                ?: return Result.Error("Empty server response")

            Result.Success(body.data)

        } catch (e: Exception) {
            Result.Error(mapNetworkError(e))
        }
    }

    /* ---------------- ERROR MAPPER ---------------- */

    private fun mapNetworkError(e: Exception): String {
        return when (e) {
            is UnknownHostException ->
                "No internet connection."

            is SocketTimeoutException ->
                "Connection timed out. Please try again."

            is IOException ->
                "Network error. Please check your connection."

            else ->
                "Something went wrong. Please try again."
        }
    }
}
