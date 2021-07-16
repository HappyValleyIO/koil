package org.koil.config

import org.koil.auth.AuthAuthority
import org.koil.auth.AuthRole
import org.koil.user.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
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
class SecurityConfig(
    private val userDetailsService: UserServiceImpl,
    private val passwordEncoder: PasswordEncoder,
    private val switchUserFilter: SwitchUserFilter,
    @Value("\${auth.remember-me.key}") private val key: String,
    private val details: UserDetailsService,
    private val persistence: PersistentTokenRepository
) : WebSecurityConfigurerAdapter() {

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
            .addFilterAfter(switchUserFilter, FilterSecurityInterceptor::class.java)
            .authorizeRequests()
            .antMatchers("/admin/impersonation/logout").hasAuthority(AuthRole.ADMIN_IMPERSONATING_USER.name)
            .antMatchers("/admin/**").hasAuthority(AuthAuthority.ADMIN.name)
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
