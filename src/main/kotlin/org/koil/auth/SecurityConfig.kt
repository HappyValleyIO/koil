package org.koil.auth

import org.koil.user.UserServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.CacheControl
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.util.concurrent.TimeUnit

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userDetailsService: UserServiceImpl,
    private val passwordEncoder: PasswordEncoder,
    private val switchUserFilter: SwitchUserFilter
) :
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
            .addFilterAfter(switchUserFilter, FilterSecurityInterceptor::class.java)
            .authorizeRequests()
            .antMatchers("/admin/impersonation/logout").hasAuthority(AuthRole.ADMIN_IMPERSONATING_USER.name)
            .antMatchers("/admin/**").hasAuthority(AuthAuthority.ADMIN.name)
            .antMatchers("/dashboard/**").authenticated()
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

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/")
            .setCacheControl(
                CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic()
            )
    }
}
