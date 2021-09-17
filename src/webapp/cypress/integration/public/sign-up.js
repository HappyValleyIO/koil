const sizes = ['iphone-12', 'ipad-mini', 'macbook-13'];

sizes.forEach(size => {

    describe(`User sign up flows on ${size}`, () => {

        beforeEach(() => {
            cy.viewport(size)
        })

        it(`should pass the user details from the homepage to the signup page`, () => {
            cy.visit("/");
            cy.get('[data-test=sign-up-email]').type('test@getkoil.dev{enter}');
            cy.get('[data-test=register-form]').within(() => {
                cy.get('input[name=email]').should('have.value', 'test@getkoil.dev')
            })
        });

        it(`should accept a new user sign up for valid email and password`, () => {
            cy.visit("/auth/register")
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

        it('should show password when toggled', () => {
            cy.visit('/auth/register')

            cy.get('[data-test=register-form]').within(() => {
                cy.get('input[name=password]').should('have.attr', 'type', 'password')
                    .type('SomePass')
                cy.get('[data-test=toggle-password]').click()
                cy.get('input[name=password]').should('have.attr', 'type', 'text')
            })
        })

        it(`should return an error when email already taken`, () => {
            cy.createRandomAccount()
            cy.clearCookies()
            cy.get('@account').then(account => {
                cy.visit("/auth/register")
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
});
