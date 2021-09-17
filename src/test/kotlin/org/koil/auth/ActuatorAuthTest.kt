package org.koil.auth

import org.junit.jupiter.api.Test
import org.koil.BaseIntegrationTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.test.web.servlet.get

class ActuatorAuthTest : BaseIntegrationTest() {

    @Test
    internal fun `prometheus user can access endpoint`() {
        mockMvc.get("/actuator/prometheus") {
            with(httpBasic("prometheus", "prometheus"))
        }.andExpect {
            status {
                isOk()
            }
        }
    }

    @Test
    internal fun `unautheticated user can access other actuator endpoints`() {
        mockMvc.get("/actuator/prometheus")
            .andExpect {
                status {
                    isUnauthorized()
                }
            }

        mockMvc.get("/actuator/health")
            .andExpect {
                status {
                    isOk()
                }
            }
    }
}
