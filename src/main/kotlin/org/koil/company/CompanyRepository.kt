package org.koil.company

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface CompanyRepository : PagingAndSortingRepository<Company, Long>{

    fun findCompanyBySignupLink(signupLink: UUID): Company?
}
