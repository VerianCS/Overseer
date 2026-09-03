package com.sideplanetary.overseerPlugin.collector

import com.enderstorage.sentinel.dto.BlockBreakTelemetry
import java.util.concurrent.ArrayBlockingQueue

class BoundedTelemetryQueue(private val capacity: Int = 5000) {

    private val queue = ArrayBlockingQueue<BlockBreakTelemetry>(capacity)

    fun offer(event: BlockBreakTelemetry) {
        // Drop the oldest record to guarantee zero heap exhaustion if full
        while (!queue.offer(event)) {
            queue.poll()
        }
    }

    fun drainTo(target: MutableList<BlockBreakTelemetry>, maxElements: Int): Int {
        return queue.drainTo(target, maxElements)
    }

    fun isEmpty(): Boolean = queue.isEmpty()
}
