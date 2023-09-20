package com.huaguang.flowoftime

import com.huaguang.flowoftime.ui.pages.time_record.EventControlViewModel
import com.huaguang.flowoftime.ui.pages.time_record.event_buttons.EventButtonsViewModel
import com.huaguang.flowoftime.ui.pages.time_record.time_regulator.TimeRegulatorViewModel

data class AppViewModels(
    val eventControlViewModel: EventControlViewModel,
    val buttonsViewModel: EventButtonsViewModel,
    val regulatorViewModel: TimeRegulatorViewModel,
)