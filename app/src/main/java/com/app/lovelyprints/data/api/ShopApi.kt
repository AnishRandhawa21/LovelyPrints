package com.app.lovelyprints.data.api

import com.app.lovelyprints.data.model.PrintOptions
import com.app.lovelyprints.data.model.ShopListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ShopApi {

    @GET("students/shops")
    suspend fun getShops(): Response<ShopListResponse>

    @GET("students/shops/{shopId}/print-options")
    suspend fun getPrintOptions(
        @Path("shopId") shopId: String
    ): Response<PrintOptions>

}
