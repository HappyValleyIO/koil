package org.koil.auth

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.random.Random

class RegistrationControllerTest : BaseIntegrationTest() {
    companion object {
        const val registerEndpoint = "/auth/register"
    }

    @Test
    internal fun `GIVEN user is already logged in WHEN visiting register page THEN redirect to dashboard`() {
        withTestSession { session ->
            mockMvc.get(registerEndpoint) {
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
    internal fun `GIVEN user is not logged in WHEN attempting to register with bad input THEN return bad request`() {
        mockMvc.post(registerEndpoint) {
            with(csrf())
            param("email", "${Random.nextInt()}")
            param("handle", "t")
            param("password", "abc")
            param("name", "")
        }.andExpect {
            status {
                is4xxClientError()
            }
            model {
                attributeHasFieldErrors("submitted", "password", "handle", "name", "email")
            }
        }
    }
}
