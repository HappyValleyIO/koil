const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];

sizes.forEach(size => {

  describe(`Admin access on ${size}`, () => {

    beforeEach(() => {
      cy.viewport(size)
      cy.createRandomAccount()
      cy.clearCookies()
      cy.visit("/auth/login")
    })

    it('should show the admin links to an admin on the normal dashboard', () => {
      cy.get('[data-test=login-email-input]').type('admin@getspringboard.dev')
      cy.get('[data-test=login-password-input]').type('SecurePass123!')
      cy.get('[data-test=login-submit]').click()

      if (size === 'macbook-13') {
        cy.get('[data-test=navbar-admin-link]').should('be.visible')
        cy.get('[data-test=dashboard-menu-admin-link]').should('be.visible')
      } else {
        cy.get('[data-test=menu-button]').click()
        cy.get('[data-test=dashboard-menu-admin-link]').should('be.visible')
      }
    })

    it('should not show the admin links to an admin on the normal dashboard', () => {
      cy.createRandomAccountAndLogin()

      if (size === 'macbook-13') {
        cy.get('[data-test=navbar-admin-link]').should('not.be.visible')
        cy.get('[data-test=dashboard-menu-admin-link]').should('not.be.visible')
      } else {
        cy.get('[data-test=menu-button]').click()
        cy.get('[data-test=dashboard-menu-admin-link]').should('not.be.visible')
      }
    })

    it(`should login successfully`, () => {
      cy.get('[data-test=login-email-input]').type('admin@getspringboard.dev')
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
