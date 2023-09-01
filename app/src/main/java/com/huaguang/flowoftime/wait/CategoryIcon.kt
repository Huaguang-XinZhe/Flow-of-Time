package com.huaguang.flowoftime.wait

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CategoryIcon(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryName: String,
    @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val icon: ByteArray
) {
    // 在数据类中具有 Array 类型的属性编译器建议重写 equals 和 hashCode 方法，重新由编译器自动完成
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CategoryIcon

        if (id != other.id) return false
        if (categoryName != other.categoryName) return false
        if (!icon.contentEquals(other.icon)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + categoryName.hashCode()
        result = 31 * result + icon.contentHashCode()
        return result
    }
}

