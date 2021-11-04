package org.koil.dev

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import java.io.InputStream
import java.util.*
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeMessage

data class SentEmail(val to: String, val subject: String, val body: String)

class LoggingMailSender : JavaMailSender {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(LoggingMailSender::class.java)!!
    }

    private val sentEmails: MutableList<SentEmail> = mutableListOf()

    fun getEmails(): List<SentEmail> = sentEmails


    override fun createMimeMessage(): MimeMessage {
        return MimeMessage(Session.getDefaultInstance(Properties()))
    }

    override fun createMimeMessage(contentStream: InputStream): MimeMessage {
        return MimeMessage(Session.getDefaultInstance(Properties()), contentStream)
    }

    override fun send(mimeMessage: MimeMessage) {
        sentEmails.add(SentEmail(mimeMessage.getRecipients(Message.RecipientType.TO).joinToString(","), mimeMessage.subject.toString(), mimeMessage.content.toString() ))
        LOG.info("Sending email $mimeMessage")
    }

    override fun send(vararg mimeMessages: MimeMessage?) {
        mimeMessages.forEach {
            it?.also{ message ->
                sentEmails.add(SentEmail(message.getRecipients(Message.RecipientType.TO).joinToString(","), message.subject.toString(), message.content.toString() ))
            }
            LOG.info("Sending email $it")
        }
    }

    override fun send(mimeMessagePreparator: MimeMessagePreparator) {
        LOG.info("Sending email $mimeMessagePreparator")
    }

    override fun send(vararg mimeMessagePreparators: MimeMessagePreparator?) {
        mimeMessagePreparators.forEach {
            LOG.info("Sending email $it")
        }
    }

    override fun send(simpleMessage: SimpleMailMessage) {
        sentEmails.add(SentEmail(simpleMessage.to.orEmpty().joinToString(","), simpleMessage.subject.toString(), simpleMessage.text.orEmpty() ))
        LOG.info("Sending email $simpleMessage")
    }

    override fun send(vararg simpleMessages: SimpleMailMessage?) {
        simpleMessages.forEach {
            it?.also{ message ->
                sentEmails.add(SentEmail(message.to.contentToString(), message.subject.toString(), message.text.orEmpty() ))
            }
            LOG.info("Sending email $it")
        }
    }
}
