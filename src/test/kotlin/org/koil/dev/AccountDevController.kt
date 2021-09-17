package org.koil.dev

import org.koil.user.Account
import org.koil.user.AccountRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dev")
class AccountDevController(@Autowired private val accountRepository: AccountRepository) {
    @GetMapping("/account")
    fun byEmail(
        @RequestParam("email") email: String
    ): Account {
        return accountRepository.findAccountByEmailAddressIgnoreCase(email)!!
    }
}
