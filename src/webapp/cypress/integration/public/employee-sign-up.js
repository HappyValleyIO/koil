import {sizes} from "../../support/sizes";

sizes.forEach(size => {

    describe(`Individual User sign up flows on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
        })

        describe(`with valid signup url`, () => {

            it(`should accept a new user sign up for valid email and password`, () => {
                cy.getCompanySignupLink().then( signupLink => {
                    cy.visit("/auth/register/individual?signupLink="+signupLink)
                    const slug = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 5);
                    cy.get('[data-test=register-form]').within(() => {
                        cy.get('input[name=name]').type('Test User');
                        cy.get('input[name=handle]').type(slug);
                        cy.get('input[name=email]').type(`test+${slug}@getkoil.dev`);
                        cy.get('input[name=password]').type('SomeSecurePass123?!');

                        cy.get('button[type=submit]').click();
                        cy.url().should('include', '/dashboard')
                    })
                })

            })

            it('should show password when toggled', () => {
                cy.getCompanySignupLink().then( signupLink => {
                    cy.visit("/auth/register/individual?signupLink=" + signupLink)

                    cy.get('[data-test=register-form]').within(() => {
                        cy.get('input[name=password]').should('have.attr', 'type', 'password')
                            .type('SomePass')
                        cy.get('[data-test=toggle-password]').click()
                        cy.get('input[name=password]').should('have.attr', 'type', 'text')
                    })
                })
            })

            it(`should return an error when email already taken`, () => {
                cy.getCompanySignupLink().then( signupLink => {
                    cy.createRandomAccount()
                    cy.clearCookies()
                    cy.get('@account').then(account => {
                        cy.visit("/auth/register/individual?signupLink="+signupLink)
                        const slug = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 5);
                        cy.get('[data-test=register-form]').within(() => {
                            cy.get('[data-test=email-error]').should('not.exist')

                            cy.get('input[name=name]').type('Test User');
                            cy.get('input[name=handle]').type(slug);
                            cy.get('input[name=email]').type(account.email);
                            cy.get('input[name=password]').type('SomeSecurePass123?!');

                            cy.get('button[type=submit]').click()
                        })

                        cy.get('[data-test=email-error]').should('exist')
                    })
                })
            })
        })

        describe (`with no signup url`, () => {
            it(`should redirect to the company registration page`, () => {
                cy.visit("/auth/register/individual")
                cy.url().should('include', '/auth/register/company')
            })
        })
    })
});
