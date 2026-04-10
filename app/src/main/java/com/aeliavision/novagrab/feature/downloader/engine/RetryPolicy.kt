package com.aeliavision.novagrab.feature.downloader.engine

import com.aeliavision.novagrab.core.network.NetworkMonitor
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import java.io.IOException

class RetryPolicy @Inject constructor(
    private val networkMonitor: NetworkMonitor,
) {
    data class Config(
        val maxAttempts: Int = 5,
        val initialDelayMs: Long = 1000L,
        val maxDelayMs: Long = 60_000L,
        val factor: Double = 2.0,
        val jitter: Boolean = true,
    )

    suspend fun <T> withRetry(
        config: Config = Config(),
        block: suspend () -> T,
    ): T {
        var lastException: Exception? = null
        var delayMs = config.initialDelayMs

        repeat(config.maxAttempts) { attempt ->
            try {
                return block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: RangeNotSupportedException) {
                throw e
            } catch (e: Exception) {
                lastException = e

                if (attempt == config.maxAttempts - 1) throw e

                if (e is IOException && !networkMonitor.isConnected()) {
                    networkMonitor.awaitConnection()
                }

                val actualDelay = if (config.jitter) {
                    delayMs + (0..delayMs / 4).random()
                } else {
                    delayMs
                }

                delay(actualDelay.coerceAtMost(config.maxDelayMs))
                delayMs = (delayMs * config.factor).toLong().coerceAtMost(config.maxDelayMs)
            }
        }

        throw lastException ?: Exception("Retry failed")
    }
}
