package org.koil.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.koil.BaseIntegrationTest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.web.server.LocalServerPort
import java.io.File
import java.io.InputStream
import java.io.SequenceInputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.streams.toList

/**
 * Note that this test class is **not** transactional. This means that the data will persist in the test database between runs.
 *
 * This is because it's hard to implement well. The tests have no idea how cypress will call the system so it's hard to
 * have rollback happen based off of several ostensibly disconnected web requests.
 */
@Execution(ExecutionMode.CONCURRENT)
class CypressIntegrationTest : BaseIntegrationTest() {
    companion object {
        val logger: Logger = LoggerFactory.getLogger(CypressIntegrationTest::class.java)
    }

    @LocalServerPort
    var port = 0

    @TestFactory
    fun runCypressTests(): Collection<DynamicTest> {
        val threads = max(Runtime.getRuntime().availableProcessors() / 2, 1)
        // If this breaks then it's likely that there are multiple node installations in .gradle/nodejs for this project
        val nodeDirectory = Files.list(Paths.get("./.gradle/nodejs")).toList().last()
        val npx = Paths.get(nodeDirectory.toString(), "bin/npx").toAbsolutePath()

        val basePath = "./build/webapp/cypress/integration/"

        val files = Files.walk(Paths.get(basePath))
            .filter { Files.isRegularFile(it) }
            .map { it.toString().substringAfter(basePath) }
            .toList()

        return files.windowed(4, 4, true)
            .map { names ->
                val spec = names.joinToString(",") { "cypress/integration/$it" }
                DynamicTest.dynamicTest(spec) {
                    val start = System.currentTimeMillis()
                    val process = ProcessBuilder()
                        .directory(File("./build/webapp/"))
                        .command(
                            "/bin/bash",
                            "-c",
                            """CYPRESS_BASE_URL=http://localhost:$port "$npx" cypress run --spec $spec"""
                        )
                        .start()

                    val streamOutput: InputStream = SequenceInputStream(process.inputStream, process.errorStream)

                    process.waitFor(15, TimeUnit.MINUTES)
//                    streamOutput.transferTo(System.out)

                    logger.info("TIME TAKEN FOR [$spec] is [${System.currentTimeMillis() - start}ms]")
                    assertEquals(0, process.exitValue()) {
                        """
                                PROCESS EXIT CODE: ${process.exitValue()}
                                                    ${streamOutput.bufferedReader().lines().toList().joinToString("\n")}
                            """
                    }
                }
            }
            .toList()
    }
}
