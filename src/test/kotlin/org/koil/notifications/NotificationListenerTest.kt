package org.koil.notifications

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.koil.user.Account
import org.koil.user.AccountUpdateEvent
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationEventPublisher

internal class NotificationListenerTest : BaseIntegrationTest() {

    @Autowired
    lateinit var publisher: ApplicationEventPublisher

    @MockBean
    lateinit var notificationService: EmailNotificationService

    @Test
    internal fun `WHEN an account update event is received THEN send an alert`() {
        withTestAccount { account: Account ->
            publisher.publishEvent(AccountUpdateEvent(this, account))

            Mockito.verify(notificationService, Mockito.times(1))
                .sendAccountUpdateConfirmation(account)
        }
    }

    @Test
    internal fun `WHEN an account creation event is received THEN send an alert`() {
        withTestAccount { account: Account ->
            Mockito.verify(notificationService, Mockito.times(1))
                .sendAccountCreationConfirmation(account)
        }
    }
}
