package network.cere.ddc.contract.model.response

import network.cere.ddc.contract.model.Balance
import network.cere.ddc.contract.model.Schedule

data class Account(
    val deposit: Balance,
    val payableSchedule: Schedule
)
