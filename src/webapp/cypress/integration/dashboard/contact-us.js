describe(`User contact us page`, () => {
    beforeEach(() => {
        cy.createRandomAccount()
    })

    it(`should load the page`, () => {
        cy.visit('/dashboard/contact-us')
    })
})
