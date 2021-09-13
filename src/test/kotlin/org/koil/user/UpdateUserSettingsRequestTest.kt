package org.koil.user

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.koil.fixtures.AccountFixtures

internal class UpdateUserSettingsRequestTest {
    @Test
    internal fun `updates existing account as expected`() {
        val account = AccountFixtures.existingAccount

        val request = UpdateUserSettingsRequest(
            name = "Updated User",
            email = "updated@example.com",
            weeklySummary = !account.notificationSettings.weeklyActivity,
            updateOnAccountChange = !account.notificationSettings.emailOnAccountChange
        )

        assertThat(request.update(account)).isEqualTo(
            account.copy(
                fullName = request.name,
                emailAddress = request.email,
                notificationSettings = NotificationSettings(
                    weeklyActivity = request.weeklySummary!!,
                    emailOnAccountChange = request.updateOnAccountChange!!
                )
            )
        )
    }
}
