package org.springboard

import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver
import org.jsoup.Jsoup
import org.springboard.auth.AuthAuthority
import org.springboard.user.EnrichedUserDetails
import org.springboard.user.InternalUser
import org.springboard.user.UserQueryResult
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.test.util.AssertionErrors
import org.springframework.web.servlet.ModelAndView
import org.testcontainers.containers.GenericContainer
import java.io.PrintWriter
import java.time.Instant
import java.util.*

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

fun PebbleViewResolver.render(mav: ModelAndView): String {
    this.setSuffix(".peb")
    val view = this.resolveViewName(mav.viewName!!, Locale.ENGLISH)

    val mockResp = MockHttpServletResponse()
    view!!.render(mav.model, MockHttpServletRequest(), mockResp)

    return mockResp.contentAsString
}

fun testUserDetails(email: String = "test+${Random().nextInt()}@getspringboard.dev",
                    password: String = "TestPass123!",
                    authorities: List<AuthAuthority> = listOf(AuthAuthority.USER)
): EnrichedUserDetails {
    val id = Random().nextLong()
    return EnrichedUserDetails(
            User.builder()
                    .username(email)
                    .password(password)
                    .authorities(authorities.map { SimpleGrantedAuthority(it.name) })
                    .build(), id, id.toString()
    )
}

fun String.asResource(): String {
    return ClassPathResource(this).file.readText()
}

fun PebbleViewResolver.regressionTestTemplate(
        mav: ModelAndView,
        recorderFileName: String,
        description: String,
        rewrite: Boolean = false
) {
    val current = {
        val rendered = this.render(mav)
        Jsoup.parse(rendered).outerHtml()
    }()

    if (rewrite) {
        val pw = PrintWriter("src/test/resources/$recorderFileName")
        pw.write(current)
        pw.close()
    } else {
        val previousRun = {
            val raw = recorderFileName.asResource()
            Jsoup.parse(raw).outerHtml()
        }()

        try {
            AssertionErrors.assertEquals(description, previousRun, current)
        } catch (e: AssertionError) {
            val pw = PrintWriter("src/test/resources/$recorderFileName.diff")
            pw.write(current)
            pw.close()
            throw e
        }
    }
}

fun internalTestUser(authorities: List<AuthAuthority> = listOf(AuthAuthority.ADMIN)): InternalUser {
    return UserQueryResult(0, "test@example.com", "SomePass123!", Instant.now(),
            "Test User", "user", null, authorities, UUID.randomUUID())
}
