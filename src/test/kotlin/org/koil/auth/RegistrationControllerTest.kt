package org.koil.auth

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*
import kotlin.random.Random

class RegistrationControllerTest : BaseIntegrationTest() {
    companion object {
        const val registerIndividualEndpoint = "/auth/register/individual"
        const val registerCompanyEndpoint = "/auth/register/company"
    }

    @Test
    internal fun `GIVEN user is already logged in WHEN visiting individual register page THEN redirect to dashboard`() {
        withTestSession { session ->
            mockMvc.get(registerIndividualEndpoint) {
                with(SecurityMockMvcRequestPostProcessors.user(session))
            }.andExpect {
                status {
                    is3xxRedirection()
                }

                header {
                    string("location", "/dashboard")
                }
            }
        }
    }

    @Test
    internal fun `GIVEN user is already logged in WHEN visiting company register page THEN redirect to dashboard`() {
        withTestSession { session ->
            mockMvc.get(registerCompanyEndpoint) {
                with(SecurityMockMvcRequestPostProcessors.user(session))
            }.andExpect {
                status {
                    is3xxRedirection()
                }

                header {
                    string("location", "/dashboard")
                }
            }
        }
    }

    @Test
    internal fun `GIVEN user is not logged in WHEN attempting to register as individual with bad input THEN return bad request`() {
        mockMvc.post(registerIndividualEndpoint) {
            with(csrf())
            param("email", "${Random.nextInt()}")
            param("handle", "t")
            param("password", "abc")
            param("name", "")
            param("signupLink", UUID.randomUUID().toString())
        }.andExpect {
        }.andExpect {
            status {
                is4xxClientError()
            }
            model {
                attributeHasFieldErrors("submitted", "password", "handle", "name", "email")
            }
        }
    }

    @Test
    internal fun `GIVEN user is not logged in WHEN attempting to register as company with bad input THEN return bad request`() {
        mockMvc.post(registerCompanyEndpoint) {
            with(csrf())
            param("email", "${Random.nextInt()}")
            param("handle", "t")
            param("password", "abc")
            param("name", "")
            param("companyName", "")
            param("signupLink", UUID.randomUUID().toString())
        }.andExpect {
        }.andExpect {
            status {
                is4xxClientError()
            }
            model {
                attributeHasFieldErrors("submitted", "password", "handle", "name", "email", "companyName")
            }
        }
    }
}
