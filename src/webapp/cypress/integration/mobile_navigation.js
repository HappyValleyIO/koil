const sizes = ['iphone-6', 'iphone-x'];

sizes.forEach(size => {
    describe(`User can open mobile menu on ${size}`, () => {
        beforeEach(() => {
            cy.createRandomAccountAndLogin()
            cy.viewport(size)
        })

        it(`should show mobile menu on button click`, () => {
            cy.get('[data-test=mobile-navbar]').should('not.be.visible')
            cy.get('[data-test=menu-button]').click()
            cy.get('[data-test=mobile-navbar]').should('be.visible')
        });

        it(`should show mobile menu on dashboard`, () => {
            cy.get('[data-test=mobile-navbar]').should('not.be.visible')
            cy.get('[data-test=menu-button]').click()
            cy.get('[data-test=mobile-navbar]').should('be.visible')
        });
    });
})
