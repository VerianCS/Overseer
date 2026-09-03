package com.enderstorage.overseer.controller

import com.enderstorage.overseer.dto.MapMarkerDTO

import com.enderstorage.overseer.dto.OverviewStatsDTO

import com.enderstorage.overseer.dto.ThreatAlertDTO

import com.enderstorage.overseer.entity.Severity

import com.enderstorage.overseer.repository.AnomalyAlertRepository

import com.enderstorage.overseer.repository.MiningEventRepository

import org.springframework.data.domain.PageRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.time.temporal.ChronoUnit

@RestController
@RequestMapping("/api/v1/deck")
class ObservationDeckController(
    private val miningEventRepository: MiningEventRepository,
    private val anomalyAlertRepository: AnomalyAlertRepository
) {

    /**
     * Map View Endpoint: Retrieves mining markers within a spatial bounding box.
     * GET /api/v1/deck/map?world=world&minX=-500&maxX=500&minZ=-500&maxZ=500&minutesBack=60
     */
    @GetMapping("/map")
    fun getSpatialEvents(
        @RequestParam(defaultValue = "world") world: String,
        @RequestParam minX: Int,
        @RequestParam maxX: Int,
        @RequestParam minZ: Int,
        @RequestParam maxZ: Int,
        @RequestParam(defaultValue = "60") minutesBack: Long,
        @RequestParam(defaultValue = "1000") limit: Int
    ): ResponseEntity<List<MapMarkerDTO>> {
        val since = Instant.now().minus(minutesBack, ChronoUnit.MINUTES)
        val pageable = PageRequest.of(0, limit.coerceAtMost(2500))

        val events = miningEventRepository.findInBoundingBox(
            world = world,
            minX = minX,
            maxX = maxX,
            minZ = minZ,
            maxZ = maxZ,
            since = since,
            pageable = pageable
        ).map { MapMarkerDTO.from(it) }

        return ResponseEntity.ok(events)
    }

    /**
     * Threat Log Feed: Retrieves diagnostic anomaly alerts for the tactical sidebar.
     * GET /api/v1/deck/alerts?severity=CRITICAL&limit=50
     */
    @GetMapping("/alerts")
    fun getThreatAlerts(
        @RequestParam(required = false) severity: Severity?,
        @RequestParam(defaultValue = "50") limit: Int
    ): ResponseEntity<List<ThreatAlertDTO>> {
        val pageable = PageRequest.of(0, limit.coerceAtMost(200))

        val alerts = if (severity != null) {
            anomalyAlertRepository.findBySeverityOrderByCreatedAtDesc(severity, pageable)
        } else {
            anomalyAlertRepository.findByOrderByCreatedAtDesc(pageable)
        }.map { ThreatAlertDTO.from(it) }

        return ResponseEntity.ok(alerts)
    }

    /**
     * Overview Counters: Quick status statistics for the top navigation bar.
     * GET /api/v1/deck/stats
     */
    @GetMapping("/stats")
    fun getOverviewStats(): ResponseEntity<OverviewStatsDTO> {
        val totalEvents = miningEventRepository.count()
        val totalAlerts = anomalyAlertRepository.count()
        val criticalAlerts = anomalyAlertRepository.countBySeverity(Severity.CRITICAL)

        return ResponseEntity.ok(
            OverviewStatsDTO(
                totalEventsLogged = totalEvents,
                activeThreatsCount = totalAlerts,
                criticalThreatsCount = criticalAlerts
            )
        )
    }
}