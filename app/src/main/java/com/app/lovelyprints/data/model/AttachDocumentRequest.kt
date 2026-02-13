package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

data class AttachDocumentRequest(
    @SerializedName("fileKey")
    val fileKey: String,

    @SerializedName("fileName")
    val fileName: String,

    @SerializedName("page_count")
    val pageCount: Int,

    val copies: Int,

    @SerializedName("paper_type_id")
    val paperTypeId: String,

    @SerializedName("color_mode_id")
    val colorModeId: String,

    @SerializedName("finish_type_id")
    val finishTypeId: String
)