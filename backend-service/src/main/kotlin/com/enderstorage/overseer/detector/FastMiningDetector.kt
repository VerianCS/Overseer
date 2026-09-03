package com.enderstorage.overseer.detector

import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import com.enderstorage.overseer.detector.MinecraftMechanicsRegistry
import com.enderstorage.overseer.entity.AlertType
import com.enderstorage.overseer.entity.AnomalyAlert
import com.enderstorage.overseer.entity.Severity
import org.springframework.stereotype.Component
import kotlin.math.ceil

@Component
class FastMiningDetector {

    // Latency tolerance buffer in ms (accounts for jitter / server TPS dip)
    private val networkToleranceMs = 75

    fun evaluate(telemetry: BlockBreakTelemetry): AnomalyAlert? {
        val delta = telemetry.breakDeltaMs ?: return null

        // Creative or instant-break blocks are dismissed
        val hardness = MinecraftMechanicsRegistry.getHardness(telemetry.blockType)
        if (hardness <= 0f) return null

        val theoreticalMs = calculateMinimumBreakTimeMs(telemetry, hardness)

        // Threshold check: Has the physical boundary been violated?
        if (delta < (theoreticalMs - networkToleranceMs)) {
            val ratio = delta.toDouble() / theoreticalMs.toDouble()
            val severity = when {
                ratio < 0.35 -> Severity.CRITICAL
                ratio < 0.60 -> Severity.HIGH
                else -> Severity.MEDIUM
            }

            return AnomalyAlert(
                playerId = telemetry.playerId,
                playerName = telemetry.playerName,
                alertType = AlertType.FAST_MINING_TEMPORAL_BREACH,
                severity = severity,
                diagnosticData = "Actual: ${delta}ms, Theoretical Min: ${theoreticalMs}ms (Speed: x${"%.2f".format(1.0 / ratio)})",
                world = telemetry.world,
                x = telemetry.x,
                y = telemetry.y,
                z = telemetry.z
            )
        }

        return null
    }

    private fun calculateMinimumBreakTimeMs(t: BlockBreakTelemetry, hardness: Float): Int {
        var baseSpeed = MinecraftMechanicsRegistry.getToolMultiplier(t.toolUsed)

        // Enchantment: Efficiency (level^2 + 1)
        if (t.toolEfficiencyLevel > 0) {
            baseSpeed += (t.toolEfficiencyLevel * t.toolEfficiencyLevel + 1)
        }

        // Potion Effect: Haste (+20% speed)
        if (t.hasHaste) {
            baseSpeed *= 1.2f
        }

        // Potion Effect: Mining Fatigue (30% speed)
        if (t.hasMiningFatigue) {
            baseSpeed *= 0.3f
        }

        // Standard can-harvest divisor = 30
        val damagePerTick = baseSpeed / hardness / 30.0f

        // Instant-mining condition
        if (damagePerTick >= 1.0f) {
            return 50 // 1 tick
        }

        val ticksRequired = ceil(1.0f / damagePerTick).toInt()
        return ticksRequired * 50
    }
}