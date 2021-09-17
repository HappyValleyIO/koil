import {sizes} from "../../support/sizes";

sizes.forEach(size => {
    describe(`Admin login on ${size}`, () => {
        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.clearCookies()
            cy.visit("/auth/login")
        })

        it(`should login successfully`, () => {
            cy.loginAsAdmin()
            cy.url().should('include', '/admin')
        });

        it(`should fail to load admin page for a non-admin`, () => {
            cy.createRandomAccountAndLogin()

            cy.url().should('include', '/dashboard')

            cy.request({
                failOnStatusCode: false,
                url: '/admin'
            }).then(response => {
                expect(response.status).to.eq(403)
            })
        })
    })
});
