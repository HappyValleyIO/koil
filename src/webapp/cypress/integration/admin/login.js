const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];

sizes.forEach(size => {
    describe(`Admin login on ${size}`, () => {
        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.clearCookies()
            cy.visit("/auth/login")
        })

        it(`should login successfully`, () => {
            cy.get('[data-test=login-email-input]').type('admin@getkoil.dev')
            cy.get('[data-test=login-password-input]').type('SecurePass123!')
            cy.get('[data-test=login-submit]').click()
            cy.url().should('include', '/dashboard')
            cy.visit('/admin')
            cy.url().should('include', '/admin')
        });

        it(`should fail to load admin page for a non-admin`, () => {
            cy.get('@account').then(account => {
                cy.get('[data-test=login-email-input]').type(account.email)
                cy.get('[data-test=login-password-input]').type(account.passwd)
                cy.get('[data-test=login-submit]').click()
                cy.url().should('include', '/dashboard')

                cy.request({
                    failOnStatusCode: false,
                    url: '/admin'
                }).then(response => {
                    expect(response.status).to.eq(403)
                })
            })
        })

    })
});
