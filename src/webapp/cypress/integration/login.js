const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];

sizes.forEach(size => {

describe(`User login flows on ${size}`, () => {

    beforeEach(() => {
      cy.viewport(size)
      cy.createRandomAccount()
      cy.clearCookies()
      cy.visit("/auth/login")
    })

    it(`should login successfully`, () => {
        cy.get('@account').then(account => {
            cy.get('[data-test=login-email-input]').type(account.email)
            cy.get('[data-test=login-password-input]').type(account.passwd)
            cy.get('[data-test=login-submit]').click()
            cy.url().should('include', '/dashboard')
        })
    });

    it('should remember when remember-me is checked on login', function () {
        cy.get('@account').then(account => {
            cy.get('[data-test=remember-me]').check()
            cy.get('[data-test=login-email-input]').type(account.email)
            cy.get('[data-test=login-password-input]').type(account.passwd)
            cy.get('[data-test=login-submit]').click()
            cy.get('[data-test=login-submit]').click()

            cy.url().should('include', 'dashboard')

            cy.clearCookie('SESSION')
            cy.visit('/dashboard')
            cy.url().should('include', 'dashboard')
        })
    })

    it(`should show failure error message`, () => {
        cy.get('@account').then(account => {
            cy.get('[data-test=bad-credentials-error]').should('not.exist')
            cy.get('[data-test=login-email-input]').type(account.email)
            cy.get('[data-test=login-password-input]').type(account.passwd + 'X')
            cy.get('[data-test=login-submit]').click()
            cy.url().should('include', '/auth/login')
            cy.get('[data-test=bad-credentials-error]').should('be.visible')
        })
    })
  })
});
