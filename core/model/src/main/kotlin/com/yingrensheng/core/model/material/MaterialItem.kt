package com.yingrensheng.core.model.material

enum class MaterialType {
    PHOTO,
    VIDEO,
    AUDIO,
    NOTE,
}

data class MaterialItem(
    val materialId: String,
    val title: String,
    val type: MaterialType,
    val durationLabel: String,
    val insight: String,
)

