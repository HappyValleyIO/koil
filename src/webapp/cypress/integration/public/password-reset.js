import {sizes} from "../../support/sizes";

sizes.forEach(size => {
    describe(`User resetting password on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.clearCookies()
        })

        it('should let user request a reset code', () => {
            cy.visit("/auth/request-password-reset")

            cy.get('[data-test=reset-form]').within(() => {
                cy.get('input[name=email]').type('admin@getkoil.dev')
                cy.get('button[type=submit]').click()
            })

            cy.get('[data-test=completed]').should('exist')
        })

        it('should return an error if email not in use', () => {
            cy.visit("/auth/request-password-reset")

            cy.get('[data-test=reset-form]').within(() => {
                cy.get('input[name=email]').type(Math.random() + 'admin@getkoil.dev')
                cy.get('button[type=submit]').click()
            })

            cy.get('[data-test=email-not-found]').should('exist')
        })

        it('should error on incorrect code', () => {
            cy.visit(`/auth/password-reset?code=5b6dcde6-05c2-4902-89f9-a5fd9b6c7c1e`)

            cy.get('[data-test=bad-credentials]').should('not.exist')

            cy.get('[data-test=reset-password]').within(() => {
                cy.get('input[name=email]').type('admin@getkoil.dev')
                cy.get('input[name=password]').type('SecurePass123!')
                cy.get('input[name=passwordConfirm]').type('SecurePass123!')
                cy.get('button[type=submit]').click()
            })

            cy.get('[data-test=bad-credentials]').should('exist')
        })

        it('should error on mismatched passwords', () => {
            cy.get('@account').then(account => {
                cy.visit("/auth/request-password-reset")

                cy.get('[data-test=reset-form]').within(() => {
                    cy.get('input[name=email]').type(account.email)
                    cy.get('button[type=submit]').click()
                })

                cy.accountDetailsForEmail(account.email).then(response => {
                    const resetCode = response.body.accountPasswordReset.resetCode
                    cy.visit(`/auth/password-reset?code=${resetCode}`)

                    cy.get('[data-test=reset-password]').within(() => {
                        cy.get('input[name=code]').should('not.be.visible')
                        cy.get('input[name=email]').type(account.email)
                        cy.get('input[name=password]').type('SecurePass123!')
                        cy.get('input[name=passwordConfirm]').type('Different123!')
                        cy.get('button[type=submit]').click()
                    })

                    cy.get('[data-test=reset-password]')
                        .should("exist")
                        .contains("Passwords don't match")
                })
            })
        })

        it('should use code to reset password', () => {
            cy.get('@account').then(account => {
                cy.visit("/auth/request-password-reset")

                cy.get('[data-test=reset-form]').within(() => {
                    cy.get('input[name=email]').type(account.email)
                    cy.get('button[type=submit]').click()
                })

                cy.accountDetailsForEmail(account.email).then(response => {
                    const resetCode = response.body.accountPasswordReset.resetCode
                    cy.visit(`/auth/password-reset?code=${resetCode}`)

                    cy.get('[data-test=reset-password]').within(() => {
                        cy.get('input[name=code]').should('not.be.visible')
                        cy.get('input[name=email]').type(account.email)
                        cy.get('input[name=password]').type('SecurePass123!')
                        cy.get('input[name=passwordConfirm]').type('SecurePass123!')
                        cy.get('button[type=submit]').click()
                    })

                    cy.get('[data-test=dashboard-index]').should('exist')
                })
            })
        })
    })
})
