package org.koil.fixtures

import org.koil.user.Account
import org.koil.user.NotificationSettings
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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
        password = BCryptPasswordEncoder().encode("TestPass123!"),
        stopDate = null,
        notificationSettings = NotificationSettings.default,
        authorities = listOf()
    )
}
