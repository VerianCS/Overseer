package com.sideplanetary.overseerPlugin.listener

import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import com.sideplanetary.overseerPlugin.collector.BoundedTelemetryQueue
import com.sideplanetary.overseerPlugin.collector.TelemetryDispatcher
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffectType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class BlockBreakListener(
    private val queue: BoundedTelemetryQueue
) : Listener {

    private val lastBreakMap = ConcurrentHashMap<UUID, Long>()

    private val criticalHardBlocks = setOf(
        Material.OBSIDIAN,
        Material.SPAWNER,
        Material.REINFORCED_DEEPSLATE,
        Material.ANVIL
    )

    private val exposedFaces = arrayOf(
        BlockFace.UP, BlockFace.DOWN,
        BlockFace.NORTH, BlockFace.SOUTH,
        BlockFace.EAST, BlockFace.WEST
    )

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val material = block.type
        val player = event.player
        val playerId = player.uniqueId
        val isOre = material.name.endsWith("_ORE") || material == Material.ANCIENT_DEBRIS

        // Discard mundane stone/dirt from network flow
        if (!isOre && !criticalHardBlocks.contains(material)) {
            return
        }

        val now = System.currentTimeMillis()
        val lastBreak = lastBreakMap.put(playerId, now)
        val breakDelta = if (lastBreak != null) (now - lastBreak).toInt() else null

        val isExposed = isBlockExposed(block)
        val mainHand = player.inventory.itemInMainHand

        // Matches your exact BlockBreakTelemetry constructor
        val telemetry = BlockBreakTelemetry(
            playerId = playerId,
            playerName = player.name,
            timestampMs = now,
            world = block.world.name,
            x = block.x,
            y = block.y,
            z = block.z,
            blockType = material.name,
            toolUsed = if (mainHand.type != Material.AIR) mainHand.type.name else null,
            toolEfficiencyLevel = mainHand.getEnchantmentLevel(Enchantment.EFFICIENCY),
            hasHaste = player.hasPotionEffect(PotionEffectType.HASTE),
            hasMiningFatigue = player.hasPotionEffect(PotionEffectType.MINING_FATIGUE),
            isExposedToAirOrCave = isExposed,
            breakDeltaMs = breakDelta
        )

        queue.offer(telemetry)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        lastBreakMap.remove(event.player.uniqueId)
    }

    private fun isBlockExposed(block: Block): Boolean {
        return exposedFaces.any { face ->
            val rel = block.getRelative(face)
            rel.type.isAir || rel.isLiquid
        }
    }
}