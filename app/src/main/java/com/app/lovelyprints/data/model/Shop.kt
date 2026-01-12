package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

/* -----------------------------
   SHOP
----------------------------- */
data class Shop(

    @SerializedName("id")
    val id: String,

    @SerializedName("shop_name")
    val shopName: String,

    @SerializedName("block")
    val block: String
)

/* -----------------------------
   PRINT OPTIONS RESPONSE
----------------------------- */

/* -----------------------------
   PAPER TYPE
----------------------------- */
data class PaperType(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("base_price")
    val basePrice: Int
)

/* -----------------------------
   COLOR MODE
----------------------------- */
data class ColorMode(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("extra_price")
    val extraPrice: Int
)

/* -----------------------------
   FINISH TYPE
----------------------------- */
data class FinishType(

    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("extra_price")
    val extraPrice: Int
)
