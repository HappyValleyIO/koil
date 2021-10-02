import {sizes} from '../../support/sizes'

sizes.forEach(size => {
    describe(`User settings page on ${size}`, () => {
        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.visit("/dashboard/user-settings")
        })

        it(`should load the user settings correctly`, () => {
            cy.get('@account').then(account => {
                cy.get('[data-test=updated]').should('not.exist')
                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('input[name=name]').should('have.value', account.name)
                    cy.get('input[name=email]').should('have.value', account.email)

                    cy.get('input[name=weeklySummary]').should('not.be.checked')
                    cy.get('input[name=updateOnAccountChange]').should('be.checked')
                })
            })
        })

        it(`should update the user settings correctly`, () => {
            cy.get('@account').then(account => {
                let updatedName = 'Updated Name'
                let updatedEmail = `updated+${account.slug}@example.com`
                cy.get('[data-test=updated]').should('not.exist')

                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('input[name=name]').clear().type(updatedName)
                    cy.get('input[name=email]').clear().type(updatedEmail)

                    cy.get('input[name=weeklySummary]').check()
                    cy.get('input[name=updateOnAccountChange]').uncheck()

                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=updated]').should('be.visible')

                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('input[name=name]').should('have.value', updatedName)
                    cy.get('input[name=email]').should('have.value', updatedEmail)

                    cy.get('input[name=weeklySummary]').should('be.checked')
                    cy.get('input[name=updateOnAccountChange]').should('not.be.checked')
                })
            })
        })

        it(`should fail to update if email in use`, () => {
            cy.get('@account').then(account => {
                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('input[name=email]').clear().type('admin@getkoil.dev')

                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=update-failed]').should('be.visible')

                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('input[name=email]').should('have.value', account.email)
                })
            })
        })
    })
});
