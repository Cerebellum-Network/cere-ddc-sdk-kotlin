package network.cere.ddc.contract.model

data class Account(
    val deposit: Balance,
    val payableSchedule: Schedule
) {
    data class Schedule(
        val rate: Balance,
        val offset: Balance
    )
}
