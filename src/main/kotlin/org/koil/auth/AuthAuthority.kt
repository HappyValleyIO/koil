package org.koil.auth

import org.springframework.security.core.authority.SimpleGrantedAuthority

enum class AuthAuthority {
    ADMIN,
    USER;

    val grantedAuthority: SimpleGrantedAuthority = SimpleGrantedAuthority(name)
}

enum class AuthRole {
    ADMIN_IMPERSONATING_USER;
}
