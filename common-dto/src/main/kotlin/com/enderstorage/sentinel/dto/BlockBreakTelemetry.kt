@file:UseSerializers(com.enderstorage.sentinel.dto.serializers.UUIDSerializer::class)

package com.enderstorage.sentinel.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.UUID

@Serializable
data class BlockBreakTelemetry(
    @field:NotNull(message = "Player UUID is required")
    val playerId: UUID,
    @field:NotBlank(message = "Player name cannot be blank")
    val playerName: String,
    val timestampMs: Long,
    @field:NotBlank(message = "World name is required")
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    @field:NotBlank(message = "Block type is required")
    val blockType: String,
    val toolUsed: String?,
    val toolEfficiencyLevel: Int,
    val hasHaste: Boolean,
    val hasMiningFatigue: Boolean,
    val isExposedToAirOrCave: Boolean,
    val breakDeltaMs: Int? = null,
)