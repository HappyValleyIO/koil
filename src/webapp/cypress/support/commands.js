// ***********************************************
// This example commands.js shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add("login", (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add("drag", { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add("dismiss", { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite("visit", (originalFn, url, options) => { ... })

function loadPageForCSRF(url) {
  return cy.request(url).its('body')
    .then(body => {
      const $html = Cypress.$(body)
      return $html.find('input[name=_csrf]').val()
    })
}

Cypress.Commands.add('createAccount', (name, username, email, passwd) => {
  loadPageForCSRF('/auth/register')
    .then(csrf => {
      cy.request({
        url: '/auth/register',
        form: true,
        method: 'POST',
        body: {
          name: name,
          email: email,
          handle: username,
          password: passwd,
          '_csrf': csrf
        }
      })
    })
})

Cypress.Commands.add('createRandomAccount', () => {
  const slug = Math.random()
    .toString(36)
    .substring(7)

  const email = `test+${slug}@getkoil.dev`
  const username = slug
  const passwd = 'SomeSecurePassword123!'
  const name = `Test User ${slug}`

  cy.createAccount(name, username, email, passwd)
    .then(() => {
      return {
          name: name,
          email: email,
          passwd: passwd,
          username: username,
          slug: slug
      }
    }).as('account')
})

Cypress.Commands.add('createRandomAccountAndLogin', () => {
  cy.createRandomAccount()
  cy.get('@account').then(() => {
    cy.visit('/dashboard')
  })
})

Cypress.Commands.add('loginAsAdmin', () => {
  cy.visit('/auth/login')
  cy.get('[data-test=login-email-input]').type('admin@getkoil.dev')
  cy.get('[data-test=login-password-input]').type('SecurePass123!')
  cy.get('[data-test=login-submit]').click()
  cy.get('[data-test=dashboard-index]').should('exist')
  cy.visit('/admin')
})
