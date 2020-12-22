package org.koil

import org.springframework.boot.runApplication

/**
 * Use this runner to start the project in dev mode. This is a local profile that's used for development and testing.
 *
 * It's all a part of the test sources of the project so there's no worry about anything here finding its way into
 * production.
 */
fun main(args: Array<String>) {
    runApplication<KoilApplication>(*args) {
        setAdditionalProfiles("dev")
    }
}
