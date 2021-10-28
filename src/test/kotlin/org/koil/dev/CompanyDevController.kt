package org.koil.dev

import org.koil.company.CompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dev")
class CompanyDevController(@Autowired private val companyRepository: CompanyRepository) {
    @GetMapping("/company")
    fun defaultCompany(): String {
        return companyRepository.findAll().minByOrNull { it.startDate }!!.signupLink.toString()
    }
}
