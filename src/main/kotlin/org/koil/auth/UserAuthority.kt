package org.koil.auth

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class UserAuthority {
    ADMIN,
    COMPANY_OWNER,
    USER;

    val grantedAuthority: SimpleGrantedAuthority = SimpleGrantedAuthority(name)
}

enum class UserRole {
    ADMIN_IMPERSONATING_USER;
}
