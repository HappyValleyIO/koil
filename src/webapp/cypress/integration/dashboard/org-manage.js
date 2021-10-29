describe(`Dashboard Org pages`, () => {

    describe('a company owner', () => {
        it(`should be able to access the org pages`, () => {
            cy.createRandomCompanyAccount()
            cy.visit('/dashboard')
            cy.get('[data-test=dashboard-menu-org-link]').should('be.visible')
        })

        it(`should be able to navigate to the org management page`, () => {
            cy.createRandomCompanyAccount()
            cy.visit('/dashboard')
            cy.get('[data-test=dashboard-menu-org-link]').click()
            cy.get('[data-test=company-name]').contains('Test Company')
            cy.get('[data-test=company-signup-link]').should('be.visible')
            cy.get('@account').then(account => {
                const slug = account.slug
                cy.get('[data-test=company-name]').contains('Test Company ' + slug)
                cy.get(`[data-test="account-row-${account.email}"]`).should('be.visible')
                    .within(() => {
                        cy.get('[data-test=user-details]').click()
                        cy.url().should('include', '/dashboard/org/accounts/')
                    })
            })
        })

    })

    it('a user should not be able to access the org pages', () => {
        cy.createRandomAccount()
        cy.visit('/dashboard')
        cy.get('[data-test=dashboard-menu-org-link]').should('not.exist')
    })

    it(`an admin should not be able to access the org pages`, () => {
        cy.loginAsAdmin()
        cy.visit('/dashboard')
        cy.get('[data-test=dashboard-menu-org-link]').should('not.exist')
    })
})
