package org.koil.notifications

import com.mitchellbosecke.pebble.PebbleEngine
import org.springframework.stereotype.Component
import java.io.StringWriter

interface EmailViews {
    fun renderAlertSuccess(model: NotificationAlertSuccessModel): String
    fun renderWelcomeMessage(model: AccountCreationNotificationViewModel): String
    fun renderPasswordReset(model: PasswordResetViewModel): String
}

@Component
class PebbleEmailViews(val pebble: PebbleEngine) : EmailViews {
    override fun renderAlertSuccess(model: NotificationAlertSuccessModel): String {
        return pebble.renderWithModel("email/alert-success-inlined", mapOf("model" to model))
    }

    override fun renderWelcomeMessage(model: AccountCreationNotificationViewModel): String {
        return pebble.renderWithModel("email/welcome-inlined", mapOf("model" to model))
    }

    override fun renderPasswordReset(model: PasswordResetViewModel): String {
        return pebble.renderWithModel("email/password-inlined", mapOf("model" to model))
    }

    private fun PebbleEngine.renderWithModel(template: String, model: Map<String, Any?>): String {
        val writer = StringWriter()
        this.getTemplate(template).evaluate(writer, model)
        return writer.toString()
    }
}
