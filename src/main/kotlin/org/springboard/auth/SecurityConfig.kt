package org.springboard.auth

import org.springboard.user.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute
import javax.servlet.http.HttpServletRequest

@Configuration
@EnableWebSecurity
class SecurityConfig(@Autowired val userDetailsService: UserServiceImpl, val passwordEncoder: PasswordEncoder) :
        WebSecurityConfigurerAdapter() {

    @Autowired
    @Throws(java.lang.Exception::class)
    fun configureGlobal(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf()
                .csrfTokenRepository(HttpSessionCsrfTokenRepository())
                .and()
                .authorizeRequests()
                .antMatchers("/dashboard/**")
                .authenticated()
                .anyRequest().permitAll()
                .and()
                .logout()
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
                .and()
                .exceptionHandling()
                .authenticationEntryPoint { _, response, _ ->
                    response.sendRedirect("/auth/login?redirect")
                }
    }

    override fun configure(auth: AuthenticationManagerBuilder?) {
        auth?.userDetailsService(userDetailsService)
    }

    @Bean
    override fun authenticationManager(): AuthenticationManager {
        return super.authenticationManager()
    }
}

@ControllerAdvice
class CsrfControllerAdvice {
    @Autowired
    private val request: HttpServletRequest? = null

    @ModelAttribute("_csrf")
    fun appendCSRFToken(): CsrfToken {
        return request!!.getAttribute(CsrfToken::class.java.name) as CsrfToken
    }
}
