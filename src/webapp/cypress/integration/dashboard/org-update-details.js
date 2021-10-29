import {sizes} from "../../support/sizes";

sizes.forEach(size => {
    describe(`Org Owner updating user details on ${size}`, () => {
        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomCompanyAccount()
            // Need to login/logout of the company account to login as the
            cy.visit("/auth/login")
        })

        it('should allow an org owner to update details for another user', () => {
            cy.get('@account').then(account => {
                cy.visit("/dashboard/org?size=10000")
                cy.get(`[data-test="account-row-${account.email}"]`)
                    .within(() => {
                        cy.get('[data-test=user-details]').click()
                    })

                cy.get('[data-test=user-details-form]').within(() => {
                    let newName = 'New Name'
                    let newEmail = `updated+${account.email}`
                    let newHandle = `${account.slug}u`
                    cy.get('input[name=fullName]').clear().type(newName)
                    cy.get('input[name=email]').clear().type(newEmail)
                    cy.get('input[name=handle]').clear().type(newHandle)
                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=update-confirmed]').should('exist')
            })
        })

        it('should return an error when email is already taken', () => {
            cy.get('@account').then(account => {
                cy.visit("/dashboard/org?size=10000")
                cy.get(`[data-test="account-row-${account.email}"]`)
                    .within(() => {
                        cy.get('[data-test=user-details]').click()
                    })

                cy.get('[data-test=user-details-form]').within(() => {
                    cy.get('input[name=email]').clear().type('admin@getkoil.dev')
                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=update-confirmed]').should('not.exist')
                cy.get('[data-test=email-taken]').should('exist')
            })
        })
    })
});
