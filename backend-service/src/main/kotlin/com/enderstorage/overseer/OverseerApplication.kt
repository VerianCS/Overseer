package com.enderstorage.overseer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OverseerApplication

fun main(args: Array<String>) {
    runApplication<OverseerApplication>(*args)
}
