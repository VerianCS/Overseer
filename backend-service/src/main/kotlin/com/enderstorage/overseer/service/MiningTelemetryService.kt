package com.enderstorage.overseer.service

import com.enderstorage.overseer.entity.MiningEvent
import com.enderstorage.overseer.repository.MiningEventRepository
import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.enderstorage.overseer.service.AnomalyDetectionService

@Service
class MiningTelemetryService(
    private val miningEventRepository: MiningEventRepository,
    private val anomalyDetectionService: AnomalyDetectionService
) {

    private val log = LoggerFactory.getLogger(MiningTelemetryService::class.java)

    @Transactional
    fun processTelemetryBatch(telemetryList: List<BlockBreakTelemetry>) {
        if (telemetryList.isEmpty()) return

        // 1. Persist raw audit events
        val entities = telemetryList.map { MiningEvent.fromDto(it) }
        miningEventRepository.saveAll(entities)

        // 2. Perform diagnostic inspection
        val alerts = anomalyDetectionService.inspect(telemetryList)

        // 3. Ready for Next.js real-time WebSocket push when wired:
        // alerts.forEach { alert -> websocketBroadcaster.send(alert) }
    }

    @Transactional
    fun processSingleTelemetry(telemetry: BlockBreakTelemetry) {
        miningEventRepository.save(MiningEvent.fromDto(telemetry))
        log.debug("Persisted single mining event for player: {}", telemetry.playerName)
    }
}