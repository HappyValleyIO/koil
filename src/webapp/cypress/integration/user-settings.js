const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];

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
                    cy.get('[data-test=name-input]').should('have.value', account.name)
                    cy.get('[data-test=email-input]').should('have.value', account.email)

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
                    cy.get('[data-test=name-input]').clear().type(updatedName)
                    cy.get('[data-test=email-input]').clear().type(updatedEmail)

                    cy.get('input[name=weeklySummary]').check()
                    cy.get('input[name=updateOnAccountChange]').uncheck()

                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=updated]').should('be.visible')

                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('[data-test=name-input]').should('have.value', updatedName)
                    cy.get('[data-test=email-input]').should('have.value', updatedEmail)

                    cy.get('input[name=weeklySummary]').should('be.checked')
                    cy.get('input[name=updateOnAccountChange]').should('not.be.checked')
                })
            })
        })

        it(`should fail to update if email in use`, () => {
            cy.get('@account').then(account => {
                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('[data-test=email-input]').clear().type('admin@getkoil.dev')

                    cy.get('button[type=submit]').click()
                })

                cy.get('[data-test=update-failed]').should('be.visible')

                cy.get('[data-test=user-settings]').within(() => {
                    cy.get('[data-test=email-input]').should('have.value', account.email)
                })
            })
        })
    })
});
