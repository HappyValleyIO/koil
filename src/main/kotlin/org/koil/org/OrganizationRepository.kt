package org.koil.org

import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Transactional
interface OrganizationRepository : PagingAndSortingRepository<Organization, Long>{
    fun findOrganizationBySignupLink(signupLink: UUID): Organization?

    fun findOrganizationByOrganizationId(organizationId: Long): Organization?
}
