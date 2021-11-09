import {sizes} from "../../support/sizes";

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
                cy.get('[data-test=login-form]').within(() => {
                    cy.get('input[name=email]').type(account.email)
                    cy.get('input[name=password]').type(account.passwd)
                    cy.get('button[type=submit]').click()
                })

                cy.url().should('include', '/dashboard')
            })
        });

        it('should remember when remember-me is checked on login', () => {
                cy.get('@account').then(account => {
                    cy.get('[data-test=login-form]').within(() => {
                        cy.get('input[name=email]').type(account.email)
                        cy.get('input[name=password]').type(account.passwd)
                        cy.get('input[name=remember-me]').check()
                        cy.get('button[type=submit]').click()
                    })

                    cy.get('[data-test=dashboard-index]').should('exist')
                    cy.clearCookie('SESSION')
                    cy.visit('/auth/login')
                    cy.url().should('include', 'dashboard')
                })
            }
        )

        it(`should show failure error message`, () => {
            cy.get('@account').then(account => {
                cy.get('[data-test=bad-credentials-error]').should('not.exist')

                cy.get('[data-test=login-form]').within(() => {
                    cy.get('input[name=email]').type(account.email)
                    cy.get('input[name=password]').type(account.passwd + 'X')
                    cy.get('button[type=submit]').click()
                })

                cy.url().should('include', '/auth/login')
                cy.get('[data-test=bad-credentials-error]').should('be.visible')
            })
        })
    })
});
