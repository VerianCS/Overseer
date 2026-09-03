package com.sideplanetary.overseerPlugin.config

import org.bukkit.configuration.file.FileConfiguration

data class SentinelConfig(
    val nifiUrl: String,
    val authKey: String, // <-- New authentication property
    val connectTimeoutSeconds: Long,
    val requestTimeoutSeconds: Long,
    val queueCapacity: Int,
    val batchSize: Int,
    val flushIntervalMs: Long
) {
    companion object {
        fun load(config: FileConfiguration): SentinelConfig {
            val url = config.getString("nifi.url")
                ?.takeIf { it.isNotBlank() }
                ?: error("CRITICAL: 'nifi.url' is missing in config.yml!")

            val key = config.getString("nifi.auth-key")
                ?.takeIf { it.isNotBlank() && it != "CHANGE_ME_TO_A_CRYPTOGRAPHICALLY_SECURE_TOKEN_32_CHARS" }
                ?: error("CRITICAL: 'nifi.auth-key' must be configured with a secure token in config.yml!")

            return SentinelConfig(
                nifiUrl = url,
                authKey = key,
                connectTimeoutSeconds = config.getLong("nifi.connect-timeout-seconds", 2L),
                requestTimeoutSeconds = config.getLong("nifi.request-timeout-seconds", 3L),
                queueCapacity = config.getInt("collector.queue-capacity", 5000),
                batchSize = config.getInt("collector.batch-size", 100),
                flushIntervalMs = config.getLong("collector.flush-interval-ms", 500L)
            )
        }
    }
}