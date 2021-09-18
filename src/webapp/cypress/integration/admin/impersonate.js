import {sizes} from "../../support/sizes";

sizes.forEach(size => {
    function isDesktopStyle() {
        return ['ipad-mini', 'macbook-13'].includes(size)
    }

    describe(`Admin impersonating users on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.clearCookies()
            cy.visit("/auth/login")
        })

        it('should allow an admin to impersonate another user', () => {
            cy.loginAsAdmin()
            cy.get('@account').then(account => {
                cy.visit("/admin?size=10000")
                cy.get(`[data-test="account-row-${account.email}"]`)
                    .within(() => {
                        cy.get('[data-test=user-details]').click()
                    })
                cy.get('[data-test=impersonate]').click()
                cy.get('[data-test=dashboard-index]').should('exist')

                if (isDesktopStyle()) {
                    cy.get('[data-test=user-handle]').contains(`@${account.username}`)
                    cy.get('[data-test=end-impersonation]').should('be.visible').click()
                    cy.get('[data-test=end-impersonation]').should('not.exist')
                    cy.get('[data-test=user-handle]').contains(`@DefaultAdmin`)
                } else {
                    cy.get('[data-test=menu-button]').click()
                    cy.get('[data-test=user-handle-mobile]').contains(`@${account.username}`)
                    cy.get('[data-test=mobile-navbar]').within(() => {
                        cy.get('[data-test=end-impersonation-mobile]').should('be.visible').click()
                        cy.get('[data-test=end-impersonation-mobile]').should('not.exist')
                    })

                    cy.get('[data-test=menu-button]').click()
                    cy.get('[data-test=user-handle-mobile]').contains('@DefaultAdmin')
                }
            })
        })
    })
});
