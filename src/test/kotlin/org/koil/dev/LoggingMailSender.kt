package org.koil.dev

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessagePreparator
import java.io.InputStream
import java.util.*
import javax.mail.Session
import javax.mail.internet.MimeMessage

class LoggingMailSender : JavaMailSender {
    companion object {
        val LOG: Logger = LoggerFactory.getLogger(LoggingMailSender::class.java)!!
    }

    override fun createMimeMessage(): MimeMessage {
        return MimeMessage(Session.getDefaultInstance(Properties()))
    }

    override fun createMimeMessage(contentStream: InputStream): MimeMessage {
        return MimeMessage(Session.getDefaultInstance(Properties()), contentStream)
    }

    override fun send(mimeMessage: MimeMessage) {
        LOG.info("Sending email $mimeMessage")
    }

    override fun send(vararg mimeMessages: MimeMessage?) {
        mimeMessages.forEach {
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
        LOG.info("Sending email $simpleMessage")
    }

    override fun send(vararg simpleMessages: SimpleMailMessage?) {
        simpleMessages.forEach {
            LOG.info("Sending email $it")
        }
    }
}
