package org.koil.ui

import org.apache.commons.lang3.StringUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.OutputFrame
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.regex.Matcher
import java.util.regex.Pattern


class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)
data class CypressTestResults(var numberOfTests: Int = 0, var numberOfPassingTests: Int = 0, var numberOfFailingTests: Int = 0)

class CypressContainerOutputFollower(private val countDownLatch: CountDownLatch) : Consumer<OutputFrame> {
    val results = CypressTestResults()

    companion object {
        private val NUMBER_OF_TESTS_REGEX: Pattern = Pattern.compile(".*│\\s*Tests:\\s*([0-9])*\\s*│.*")
        private val NUMBER_OF_PASSING_REGEX: Pattern = Pattern.compile(".*│\\s*Passing:\\s*([0-9])*\\s*│.*")
        private val NUMBER_OF_FAILING_REGEX: Pattern = Pattern.compile(".*│\\s*Failing:\\s*([0-9])*\\s*│.*")
    }

    override fun accept(outputFrame: OutputFrame) {
        val logLine: String = StringUtils.strip(outputFrame.utf8String)

        if (logLine.contains("Run Finished")) {
            countDownLatch.countDown()
        } else {
            storeNumberOfTestsIfMatches(logLine)
            storeNumberOfPassingTestsIfMatches(logLine)
            storeNumberOfFailingTestsIfMatches(logLine)
        }
    }

    private fun storeNumberOfTestsIfMatches(logLine: String) {
        val matcher: Matcher = NUMBER_OF_TESTS_REGEX.matcher(logLine)
        if (matcher.matches()) {
            results.numberOfTests = matcher.group(1).toInt()
        }
    }

    private fun storeNumberOfPassingTestsIfMatches(logLine: String) {
        val matcher: Matcher = NUMBER_OF_PASSING_REGEX.matcher(logLine)
        if (matcher.matches()) {
            results.numberOfPassingTests = matcher.group(1).toInt()
        }
    }

    private fun storeNumberOfFailingTestsIfMatches(logLine: String) {
        val matcher: Matcher = NUMBER_OF_FAILING_REGEX.matcher(logLine)
        if (matcher.matches()) {
            results.numberOfFailingTests = matcher.group(1).toInt()
        }
    }
}

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Execution(ExecutionMode.CONCURRENT)
class CypressIntegrationTest {
    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(CypressIntegrationTest::class.java)
    }

    @LocalServerPort
    var port = 0


    @TestFactory
    @Execution(ExecutionMode.CONCURRENT)
    fun runCypressTests(): Collection<DynamicTest> {
        Testcontainers.exposeHostPorts(port)

        return File("src/webapp/cypress/integration").list()
                .sortedByDescending { it } // So that the stripe test runs early
                .map { name ->
                    DynamicTest.dynamicTest(name) {
                        runCypressSpec("cypress/integration/$name", "chrome:stable")
                    }
                }
    }

    private fun createCypressContainer(specMatcher: String, browser: String): KGenericContainer? {
        val result: KGenericContainer = KGenericContainer("cypress/included:6.8.0")
                .withCommand("run", "--spec", "'$specMatcher'", "--browser", browser)
                .withSharedMemorySize(1024L * 1024 * 1024 * 4) // Chrome chews through memory. This is a hacky way to stop it from dying

        result.withFileSystemBind("build/webapp", "/e2e")
        result.withFileSystemBind("/tmp/cypress", "/tmp/cypress")

        result.workingDirectory = "/e2e"
        result.addEnv("CYPRESS_baseUrl", "http://host.testcontainers.internal:$port")
        return result
    }

    private fun runCypressSpec(specMatcher: String, browser: String = "chrome:stable") {
        createCypressContainer(specMatcher, browser)!!.use { container ->
            val countDownLatch = CountDownLatch(1)
            val output = mutableListOf<String>()
            val cypressContainerOutputFollower = CypressContainerOutputFollower(countDownLatch)
            container.withLogConsumer(cypressContainerOutputFollower)
            container.withLogConsumer {
                output.add(it.utf8String)
            }

            container.start()

            assertTrue(countDownLatch.await(10, TimeUnit.MINUTES)) {
                """
                    $specMatcher timed out before the test run printed END frame.
                    ${output.joinToString("\n")}
                    """.trimIndent()
            }
            assertEquals(0, cypressContainerOutputFollower.results.numberOfFailingTests) {
                output.joinToString("\n")
            }
        }
    }
}
