package org.koil.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.actuate.metrics.export.prometheus.PrometheusScrapeEndpoint
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
@Order(0)
class ActuatorSecurityConfig(
    @Value("\${management.basic-password}") private val prometheusPassword: String,
) : WebSecurityConfigurerAdapter() {
    companion object {
        const val ROLE = "PROMETEHEUS"
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.requestMatcher(EndpointRequest.to(PrometheusScrapeEndpoint::class.java))
            .authorizeRequests()
            .anyRequest()
            .hasAnyAuthority(ROLE)
            .and()
            .httpBasic()
    }

    @Throws(java.lang.Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.inMemoryAuthentication().withUser("prometheus").password("{noop}$prometheusPassword").authorities(ROLE)
    }
}
