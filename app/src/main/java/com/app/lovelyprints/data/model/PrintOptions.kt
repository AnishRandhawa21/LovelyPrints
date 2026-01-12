package com.app.lovelyprints.data.model

import com.google.gson.annotations.SerializedName

data class PrintOptions(
    @SerializedName("paper_types")
    val paperTypes: List<PaperType>,

    @SerializedName("color_modes")
    val colorModes: List<ColorMode>,

    @SerializedName("finish_types")
    val finishTypes: List<FinishType>
)
