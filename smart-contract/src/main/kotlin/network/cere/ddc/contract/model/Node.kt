package network.cere.ddc.contract.model

data class Node(
    val providerId: AccountId,
    val rentPerMonth: Balance,
    val freeResource: Long
)