const sizes = ['iphone-6', 'iphone-x', 'ipad-mini', 'macbook-13'];
sizes.forEach(size => {

    describe(`User sign up flows on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
        })

        it(`should pass the user details from the homepage to the signup page`, () => {
            cy.visit("/");
            cy.get('[data-test=sign-up-email]').type('test@getkoil.dev{enter}');
            cy.get('[data-test=register-form]').within(() => {
                cy.get('[data-test=email-input]').should('have.value', 'test@getkoil.dev')
            })
        });

        it(`should accept a new user sign up for valid email and password`, () => {
            cy.visit("/auth/register")
            const slug = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 5);
            cy.get('[data-test=register-form]').within(() => {
                cy.get('[data-test=name-input]').type('Test User');
                cy.get('[data-test=handle-input]').type(slug);
                cy.get('[data-test=email-input]').type(`test+${slug}@getkoil.dev`);
                cy.get('[data-test=password-input]').type('SomeSecurePass123?!');

                cy.get('[data-test=submit-button]').click();
                cy.url().should('include', '/dashboard')
            })
        })

        it.only(`should return an error when email already taken`, () => {
            cy.createRandomAccount()
            cy.clearCookies()
            cy.get('@account').then(account => {
                cy.visit("/auth/register")
                const slug = Math.random().toString(36).replace(/[^a-z]+/g, '').substr(0, 5);
                cy.get('[data-test=register-form]').within(() => {
                    cy.get('[data-test=email-error]').should('not.exist')

                    cy.get('[data-test=name-input]').type('Test User');
                    cy.get('[data-test=handle-input]').type(slug);
                    cy.get('[data-test=email-input]').type(account.email);
                    cy.get('[data-test=password-input]').type('SomeSecurePass123?!');

                    cy.get('[data-test=submit-button]').click()
                })

                cy.get('[data-test=email-error]').should('exist')
            })
        })
    })
});
