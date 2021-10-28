package org.koil.config

import org.koil.auth.UserAuthority
import org.koil.auth.UserRole
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository

@Configuration
@EnableWebSecurity
@Order(1)
class ApplicationSecurityConfig(
    private val passwordEncoder: PasswordEncoder,
    private val switchUserFilter: SwitchUserFilter,
    @Value("\${auth.remember-me.key}") private val key: String,
    private val details: UserDetailsService,
    private val persistence: PersistentTokenRepository
) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(details)
            .passwordEncoder(passwordEncoder)
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.csrf()
            .csrfTokenRepository(HttpSessionCsrfTokenRepository())
            .and()
            .addFilterAfter(switchUserFilter, FilterSecurityInterceptor::class.java)
            .authorizeRequests()
            .antMatchers("/admin/impersonation/logout").hasAuthority(UserRole.ADMIN_IMPERSONATING_USER.name)
            .antMatchers("/admin/**").hasAuthority(UserAuthority.COMPANY_OWNER.name)
            .antMatchers("/dashboard/**").authenticated()
            .anyRequest().permitAll()
            .and()
            .formLogin()
            .usernameParameter("email")
            .passwordParameter("password")
            .loginPage("/auth/login")
            .defaultSuccessUrl("/dashboard")
            .and()
            .rememberMe()
            .key(key)
            .rememberMeServices(PersistentTokenBasedRememberMeServices(key, details, persistence))
            .and()
            .logout()
            .logoutSuccessUrl("/auth/login?logout")
            .permitAll()
            .and()
            .exceptionHandling()
            .authenticationEntryPoint { _, response, _ ->
                response.sendRedirect("/auth/login?redirect=true")
            }
    }

}
