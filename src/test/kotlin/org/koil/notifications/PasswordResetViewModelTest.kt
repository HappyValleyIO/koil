package org.koil.notifications

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import java.util.*

class PasswordResetViewModelTest {
    @Test
    internal fun `generate the url given various baseUrls`() {
        val code = UUID.randomUUID()

        val happyPath = PasswordResetViewModel(
            defaults = EmailDefaults("Preview", "Company", "test"),
            baseUrl = "http://localhost:8080",
            appName = "Koil",
            code = code
        )

        assertThat(happyPath.resetLink).isEqualTo("http://localhost:8080/auth/password-reset?code=$code")

        val withTrailingSlash = happyPath.copy(baseUrl = "http://localhost:8080/")
        assertThat(withTrailingSlash.resetLink).isEqualTo("http://localhost:8080/auth/password-reset?code=$code")

        val withoutPort = happyPath.copy(baseUrl = "http://localhost")
        assertThat(withoutPort.resetLink).isEqualTo("http://localhost/auth/password-reset?code=$code")

        val withPath = happyPath.copy(baseUrl = "https://www.example.com/test")
        assertThat(withPath.resetLink).isEqualTo("https://www.example.com/test/auth/password-reset?code=$code")

        val withPathAndTrailingSlash = happyPath.copy(baseUrl = "https://www.example.com/test/")
        assertThat(withPathAndTrailingSlash.resetLink).isEqualTo("https://www.example.com/test/auth/password-reset?code=$code")
    }
}
