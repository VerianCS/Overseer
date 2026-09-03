package com.enderstorage.overseer.controller

import com.enderstorage.overseer.service.MiningTelemetryService
import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/telemetry")
class MiningTelemetryController(
    private val telemetryService: MiningTelemetryService,
) {

    @PostMapping("/mining/batch")
    fun receiveBatchTelemetry(
        @Valid @RequestBody telemetryBatch: List<BlockBreakTelemetry>,
    ): ResponseEntity<Void> {
        telemetryService.processTelemetryBatch(telemetryBatch)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/mining")
    fun receiveSingleTelemetry(
        @Valid @RequestBody telemetry: BlockBreakTelemetry,
    ): ResponseEntity<Void> {
        telemetryService.processSingleTelemetry(telemetry)
        return ResponseEntity.accepted().build()
    }
}
