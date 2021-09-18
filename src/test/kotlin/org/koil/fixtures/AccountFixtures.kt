package org.koil.fixtures

import org.koil.user.Account
import org.koil.user.HashedPassword
import org.koil.user.NotificationSettings
import java.time.Instant
import java.util.*

object AccountFixtures {
    val existingAccount = Account(
        accountId = 123,
        startDate = Instant.now(),
        fullName = "Test User",
        handle = "tester",
        publicAccountId = UUID.randomUUID(),
        emailAddress = "test@example.com",
        password = HashedPassword.encode("TestPass123!"),
        stopDate = null,
        notificationSettings = NotificationSettings.default,
        authorities = listOf()
    )
}
