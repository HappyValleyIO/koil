package org.koil.notifications

import org.koil.user.AccountCreationEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class NotificationListener(val notifications: NotificationService) {
    @Async
    @EventListener
    fun onAccountCreation(event: AccountCreationEvent) {
        notifications.sendAccountCreationConfirmation(event.account)
    }
}
