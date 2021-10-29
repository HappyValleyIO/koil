package org.koil.dev

import org.koil.company.CompanyRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/dev")
class CompanyDevController(@Autowired private val companyRepository: CompanyRepository) {
    @GetMapping("/company")
    fun defaultCompany(): String {
        return companyRepository.findAll().minByOrNull { it.startDate }!!.signupLink.toString()
    }

    @GetMapping("/company/{companyName}")
    fun defaultCompany(@PathVariable("companyName") companyName: String): String {
        return companyRepository.findAll().find { it.companyName == companyName }!!.signupLink.toString()
    }
}
