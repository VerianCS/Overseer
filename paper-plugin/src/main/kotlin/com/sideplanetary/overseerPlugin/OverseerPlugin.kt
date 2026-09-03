package com.sideplanetary.overseerPlugin

import com.sideplanetary.overseerPlugin.collector.BoundedTelemetryQueue
import com.sideplanetary.overseerPlugin.collector.TelemetryDispatcher
import com.sideplanetary.overseerPlugin.config.SentinelConfig
import com.sideplanetary.overseerPlugin.listener.BlockBreakListener
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.TimeUnit

class OverseerPlugin : JavaPlugin() {

    private lateinit var queue: BoundedTelemetryQueue
    private lateinit var dispatcher: TelemetryDispatcher
    private lateinit var sentinelConfig: SentinelConfig
    private var flushTask: ScheduledTask? = null

    override fun onEnable() {
        saveDefaultConfig()

        try {
            sentinelConfig = SentinelConfig.load(config)
        } catch (e: IllegalStateException) {
            logger.severe(e.message)
            server.pluginManager.disablePlugin(this)
            return
        }

        // 1. Initialize the Bounded Queue
        queue = BoundedTelemetryQueue(sentinelConfig.queueCapacity)

        // 2. Pass ALL 3 parameters to TelemetryDispatcher: (config, queue, logger)
        dispatcher = TelemetryDispatcher(sentinelConfig, queue, logger)

        // 3. Pass the QUEUE (not dispatcher) to BlockBreakListener
        server.pluginManager.registerEvents(BlockBreakListener(queue), this)

        // 4. Schedule periodic async flushing
        flushTask = Bukkit.getAsyncScheduler().runAtFixedRate(
            this,
            { _ -> dispatcher.flushBatch() },
            sentinelConfig.flushIntervalMs,
            sentinelConfig.flushIntervalMs,
            TimeUnit.MILLISECONDS
        )

        logger.info("Caenis Overseer Agent online. Ingestion target: ${sentinelConfig.nifiUrl}")
    }

    override fun onDisable() {
        // Cancel the scheduled flush task
        flushTask?.cancel()

        // Flush remaining in-memory telemetry before shutting down (NO .shutdown())
        if (::dispatcher.isInitialized) {
            dispatcher.flushBatch()
        }

        logger.info("Caenis Overseer Agent safely disengaged.")
    }
}