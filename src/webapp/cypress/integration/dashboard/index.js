describe(`Dashboard page`, () => {
    beforeEach(() => {
        cy.createRandomAccount()
    })

    it(`should should account verification result`, () => {
        cy.visit('/dashboard')
        cy.get('[data-test=account-verified]').should('not.be.exist')
        cy.get('[data-test=account-already-verified]').should('not.be.exist')
        cy.get('[data-test=incorrect-verification-code]').should('not.be.exist')

        cy.visit('/dashboard?accountVerified=true')
        cy.get('[data-test=account-verified]').should('be.exist')
        cy.get('[data-test=account-already-verified]').should('not.be.exist')
        cy.get('[data-test=incorrect-verification-code]').should('not.be.exist')

        cy.visit('/dashboard?accountAlreadyVerified=true')
        cy.get('[data-test=account-verified]').should('not.be.exist')
        cy.get('[data-test=account-already-verified]').should('be.exist')
        cy.get('[data-test=incorrect-verification-code]').should('not.be.exist')

        cy.visit('/dashboard?incorrectVerificationCode=true')
        cy.get('[data-test=account-verified]').should('not.be.exist')
        cy.get('[data-test=account-already-verified]').should('not.be.exist')
        cy.get('[data-test=incorrect-verification-code]').should('be.exist')
    })
})
