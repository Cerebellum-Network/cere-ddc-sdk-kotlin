package network.cere.ddc.contract.model

import java.math.BigDecimal
import java.math.BigInteger

@JvmInline
value class Balance(val value: BigInteger) {

    private companion object {
        val CERE_MULTIPLIER = BigDecimal(BigInteger.TEN.pow(10))
    }

    constructor(raw: BigDecimal) : this(raw.multiply(CERE_MULTIPLIER).toBigInteger())
    constructor(str: String) : this(BigDecimal(str).multiply(CERE_MULTIPLIER).toBigInteger())

    fun toDecimal() = BigDecimal(value).divide(CERE_MULTIPLIER)

    override fun toString() = toDecimal().toString()

    operator fun plus(balance: Balance) = Balance(value + balance.value)
    operator fun minus(balance: Balance) = Balance(value - balance.value)
    operator fun times(i: Long) = Balance(value * BigInteger.valueOf(i))
    operator fun div(i: Long) = Balance(value / BigInteger.valueOf(i))
}