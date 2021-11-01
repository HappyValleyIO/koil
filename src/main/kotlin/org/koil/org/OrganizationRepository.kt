package org.koil.org

import org.springframework.data.repository.PagingAndSortingRepository
import java.util.*

interface OrganizationRepository : PagingAndSortingRepository<Organization, Long> {
    fun findOrganizationBySignupLink(signupLink: UUID): Organization?

    fun findOrganizationByOrganizationId(organizationId: Long): Organization?
}
