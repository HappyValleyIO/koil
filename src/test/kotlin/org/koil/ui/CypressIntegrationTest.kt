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
        // If this breaks then it's likely that there are multiple node installations in .gradle/nodejs for this project
        val nodeDirectory = Files.list(Paths.get("./.gradle/nodejs")).toList().last()
        val npx = Paths.get(nodeDirectory.toString(), "bin/npx").toAbsolutePath()

        val basePath = "./build/webapp/cypress/integration/"

        return Files.walk(Paths.get(basePath))
            .filter { Files.isRegularFile(it) }
            .map { it.toString().substringAfter(basePath) }
            .map { name ->
                DynamicTest.dynamicTest(name) {
                    val process = ProcessBuilder()
                        .directory(File("./build/webapp/"))
                        .command(
                            "/bin/bash",
                            "-c",
                            """CYPRESS_BASE_URL=http://localhost:$port "$npx" cypress run --spec cypress/integration/$name"""
                        )
                        .start()

                    val streamOutput: InputStream = SequenceInputStream(process.inputStream, process.errorStream)
                    streamOutput.transferTo(System.out)

                    process.waitFor(15, TimeUnit.MINUTES)

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
