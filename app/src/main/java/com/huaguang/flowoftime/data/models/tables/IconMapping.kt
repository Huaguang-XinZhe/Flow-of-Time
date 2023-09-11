package com.huaguang.flowoftime.data.models.tables

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "iconMapping")
data class IconMapping(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var category: String? = null,
    val iconName: String
) {
    companion object {
        fun getUrlByIconName(iconName: String) =
            "https://image-bed-1315938829.cos.ap-nanjing.myqcloud.com/image/icons/$iconName.webp"
    }
}
