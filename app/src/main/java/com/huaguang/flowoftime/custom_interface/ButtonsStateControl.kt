package com.huaguang.flowoftime.custom_interface

import com.huaguang.flowoftime.EventType

interface ButtonsStateControl {

    fun toggleMainEnd()

    fun toggleSubEnd(type: EventType)

    fun hasSubjectExist(): Boolean
}