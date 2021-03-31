package org.koil.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.koil.BaseIntegrationTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.io.File
import kotlin.streams.toList


/**
 * Note that this test class is **not** transactional. This means that the data will persist in the test database between runs.
 *
 * This is because it's hard to implement well. The tests have no idea how cypress will call the system so it's hard to
 * have rollback happen based off of several ostensibly disconnected web requests.
 */
@Execution(ExecutionMode.CONCURRENT)
class CypressIntegrationTest: BaseIntegrationTest() {
    @LocalServerPort
    var port = 0

    @TestFactory
    fun runCypressTests(): Collection<DynamicTest> {
        return File("src/webapp/cypress/integration").list()
                .map { name ->
                    DynamicTest.dynamicTest(name) {
                        val process = ProcessBuilder()
                                .directory(File("./build/webapp/"))
                                .command("/bin/bash", "-c", "CYPRESS_BASE_URL=http://localhost:$port npx cypress run --spec cypress/integration/$name")
                                .start()

                        val lines = process.inputStream.bufferedReader().lines()
                        process.waitFor()

                        assertEquals(0, process.exitValue()) {
                            """
                                PROCESS EXIT CODE: ${process.exitValue()}
                                ${lines.toList().joinToString("\n")}
                            """
                        }
                    }
                }
    }
}
