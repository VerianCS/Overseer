package com.sideplanetary.overseerPlugin.collector

import com.google.gson.Gson
import com.sideplanetary.overseerPlugin.config.SentinelConfig
import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.Duration
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import java.util.UUID




class TelemetryDispatcher(
    private val config: SentinelConfig,
    private val queue: BoundedTelemetryQueue,
    private val logger: Logger
) {
    private val targetUri: URI = URI.create(config.nifiUrl)

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(UUID::class.java, object : TypeAdapter<UUID>() {
            override fun write(out: JsonWriter, value: UUID?) {
                if (value == null) out.nullValue() else out.value(value.toString())
            }
            override fun read(`in`: JsonReader): UUID? {
                return if (`in`.hasNext()) UUID.fromString(`in`.nextString()) else null
            }
        })
        .create()

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(config.connectTimeoutSeconds))
        .build()

    fun flushBatch() {
        if (queue.isEmpty()) return

        val batch = ArrayList<BlockBreakTelemetry>(config.batchSize)
        queue.drainTo(batch, config.batchSize)

        if (batch.isEmpty()) return

        val jsonPayload = gson.toJson(batch)

        val request = HttpRequest.newBuilder()
            .uri(targetUri)
            .header("Content-Type", "application/json")
            .header("X-Sentinel-Key", config.authKey) // <-- Pass shared secret
            .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
            .timeout(Duration.ofSeconds(config.requestTimeoutSeconds))
            .build()

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding())
            .exceptionally { ex ->
                logger.warning("NiFi ingestion fault at ${config.nifiUrl}: ${ex.message}")
                null
            }
    }
}