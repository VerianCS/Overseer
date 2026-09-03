package com.enderstorage.overseer.detector

import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import com.enderstorage.overseer.entity.AlertType
import com.enderstorage.overseer.entity.AnomalyAlert
import com.enderstorage.overseer.entity.Severity
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque

@Component
class XrayDetector {

    // Rolling window size per player
    private val windowSize = 25
    private val minSampleThreshold = 10

    // Bounded in-memory sliding window: PlayerUUID -> Deque<isExposed>
    private val playerWindows = ConcurrentHashMap<UUID, ConcurrentLinkedDeque<Boolean>>()

    fun evaluate(telemetry: BlockBreakTelemetry): AnomalyAlert? {
        if (!isValuableOre(telemetry.blockType)) return null

        val window = playerWindows.computeIfAbsent(telemetry.playerId) { ConcurrentLinkedDeque() }

        // Slide window
        window.addLast(telemetry.isExposedToAirOrCave)
        while (window.size > windowSize) {
            window.pollFirst()
        }

        if (window.size < minSampleThreshold) return null

        val totalSamples = window.size
        val hiddenCount = window.count { !it }
        val hiddenRatio = hiddenCount.toDouble() / totalSamples.toDouble()

        // Critical threshold: 80%+ unexposed ores out of the last N mining events
        if (hiddenRatio >= 0.80) {
            val severity = when {
                hiddenRatio >= 0.95 && totalSamples >= 20 -> Severity.CRITICAL
                hiddenRatio >= 0.88 -> Severity.HIGH
                else -> Severity.MEDIUM
            }

            return AnomalyAlert(
                playerId = telemetry.playerId,
                playerName = telemetry.playerName,
                alertType = AlertType.TOPOLOGICAL_OCCLUSION_XRAY,
                severity = severity,
                diagnosticData = "Occluded Ratio: ${"%.1f".format(hiddenRatio * 100)}% ($hiddenCount/$totalSamples unexposed ores)",
                world = telemetry.world,
                x = telemetry.x,
                y = telemetry.y,
                z = telemetry.z
            )
        }

        return null
    }

    private fun isValuableOre(type: String): Boolean {
        return type.contains("DIAMOND_ORE") ||
                type.contains("ANCIENT_DEBRIS") ||
                type.contains("EMERALD_ORE") ||
                type.contains("GOLD_ORE")
    }
}