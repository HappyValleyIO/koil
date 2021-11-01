package org.koil.dev

import org.koil.org.OrganizationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dev")
class OrganizationDevController(@Autowired private val organizationRepository: OrganizationRepository) {
    @GetMapping("/organization")
    fun defaultOrg(): String {
        return organizationRepository.findAll().minByOrNull { it.startDate }!!.signupLink.toString()
    }

    @GetMapping("/organization/{orgName}")
    fun findOrgByName(@PathVariable("orgName") orgName: String): String {
        return organizationRepository.findAll().find { it.organizationName == orgName }!!.signupLink.toString()
    }
}
