package network.cere.ddc.`ca-storage`.config

import java.time.Duration

data class ClientConfig(
    val retryTimes: Int = 3,
    val retryBackOff: Duration = Duration.ofMillis(200),
    val requestExpiration: Duration = Duration.ofSeconds(3),
    val requestTimeout: Duration = Duration.ofMinutes(5),
    val connectTimeout: Duration = Duration.ofMinutes(3)
)
