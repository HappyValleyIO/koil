const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];


sizes.forEach(size => {
    function isDesktopStyle() {
        return ['ipad-mini', 'macbook-13'].includes(size)
    }

    describe(`Admin access on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
            cy.createRandomAccount()
            cy.clearCookies()
            cy.visit("/auth/login")
        })

        it('should show the admin links to an admin on the normal dashboard', () => {
            cy.get('[data-test=login-email-input]').type('admin@getkoil.dev')
            cy.get('[data-test=login-password-input]').type('SecurePass123!')
            cy.get('[data-test=login-submit]').click()

            if (isDesktopStyle()) {
                cy.get('[data-test=navbar-admin-link]').should('be.visible')
                cy.get('[data-test=dashboard-menu-admin-link]').should('be.visible')
            } else {
                cy.get('[data-test=menu-button]').click()
                cy.get('[data-test=mobile-navbar]').within(() => {
                    cy.get('[data-test=navbar-admin-link]').should('be.visible')
                })
            }
        })

        it('should not show the admin links to an admin on the normal dashboard', () => {
            cy.createRandomAccountAndLogin()

            if (isDesktopStyle()) {
                cy.get('[data-test=navbar-admin-link]').should('not.exist')
                cy.get('[data-test=dashboard-menu-admin-link]').should('not.exist')
            } else {
                cy.get('[data-test=menu-button]').click()
                cy.get('[data-test=dashboard-menu-admin-link]').should('not.exist')
            }
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

        it('should allow an admin to impersonate another user', () => {
            cy.loginAsAdmin()
            cy.get('@account').then(account => {
                cy.get(`[data-test="account-row-${account.email}"]`)
                    .within(() => {
                        cy.get('[data-test=impersonate]').click()
                    })
                cy.get('[data-test=dashboard-index]').should('exist')

                if (isDesktopStyle()) {
                    cy.get('[data-test=user-handle]').contains(`@${account.username}`)
                    cy.get('[data-test=end-impersonation]').should('be.visible').click()
                    cy.get('[data-test=end-impersonation]').should('not.exist')
                    cy.get('[data-test=user-handle]').contains(`@DefaultAdmin`)
                } else {
                    cy.get('[data-test=menu-button]').click()
                    cy.get('[data-test=user-handle-mobile]').contains(`@${account.username}`)
                    cy.get('[data-test=mobile-navbar]').within(() => {
                        cy.get('[data-test=end-impersonation-mobile]').should('be.visible').click()
                        cy.get('[data-test=end-impersonation-mobile]').should('not.exist')
                    })

                    cy.get('[data-test=menu-button]').click()
                    cy.get('[data-test=user-handle-mobile]').contains('@DefaultAdmin')
                }
            })
        })
    })
});
