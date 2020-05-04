package org.springboard

import com.mitchellbosecke.pebble.spring.servlet.PebbleViewResolver
import java.io.PrintWriter
import java.util.Locale
import org.jsoup.Jsoup
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.test.util.AssertionErrors
import org.springframework.web.servlet.ModelAndView
import org.testcontainers.containers.GenericContainer

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(imageName)

fun PebbleViewResolver.render(mav: ModelAndView): String {
  this.setSuffix(".peb")
  val view = this.resolveViewName(mav.viewName!!, Locale.ENGLISH)

  val mockResp = MockHttpServletResponse()
  view!!.render(mav.model, MockHttpServletRequest(), mockResp)

  return mockResp.contentAsString
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
