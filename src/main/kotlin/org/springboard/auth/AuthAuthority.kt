package org.springboard.auth

enum class AuthAuthority(val ref: String) {
    ADMIN("ADMIN"),
    USER("USER");

    companion object {
        fun fromRef(ref: String): AuthAuthority {
            return values().first {
                it.ref == ref
            }
        }
    }
}
