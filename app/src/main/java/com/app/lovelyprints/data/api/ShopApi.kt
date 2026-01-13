package com.app.lovelyprints.data.api

import com.app.lovelyprints.data.model.ApiResponse
import com.app.lovelyprints.data.model.PrintOptions
import com.app.lovelyprints.data.model.ShopListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ShopApi {

    // matches web: GET /api/shops
    @GET("shops")
    suspend fun getShops(): Response<ShopListResponse>

    // matches web: GET /api/students/shops/:id/options
    @GET("students/shops/{shopId}/options")
    suspend fun getPrintOptions(
        @Path("shopId") shopId: String
    ): Response<ApiResponse<PrintOptions>>

}
