package org.koil.company

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("company")
data class Company(
    @Id val companyId: Long?,
    val companyName: String,
    val startDate: Instant,
    val stopDate: Instant?,
    val signupLink: UUID
)