package org.koil

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KoilApplication

fun main(args: Array<String>) {
    runApplication<KoilApplication>(*args)
}

