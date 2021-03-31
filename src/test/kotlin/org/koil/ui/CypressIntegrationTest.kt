package org.koil.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import java.io.File
import kotlin.streams.toList

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Execution(ExecutionMode.CONCURRENT)
class CypressIntegrationTest {
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
