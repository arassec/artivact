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
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })

Cypress.Commands.add('openArtivact', () => {
    cy.visit("http://localhost:9000/#")
})

Cypress.Commands.add('takeScreenshot', (selector, locale, appendix) => {
    let prefix = '';
    if (locale !== 'en') {
        prefix = '/' + locale
    }
    let suffix = selector;
    if (appendix) {
        suffix = selector + '-' + appendix;
    }
    cy.get(`[data-test=${selector}]`)
        .first()
        .should('be.visible')
        .screenshot(prefix + '/' + Cypress.currentTest.title + suffix);
})

Cypress.Commands.add('switchLanguage', (language) => {
    cy.openArtivact();
    cy.get('[data-test=system-settings-button]')
        .should('be.visible')
        .click()
        .get('[data-test=artivact-system-settings-appearance]')
        .should('be.visible')
        .click()
        .get('[data-test=appearance-config-language]')
        .should('be.visible')
        .click()
        .get('[data-test=appearance-config-language-selection]')
        .should('be.visible')
        .click()
        .get(`[data-test=appearance-config-language-selection-${language}]`)
        .should('be.visible')
        .click()

    cy.get('[data-test=appearance-configuration-page-save]')
        .click()
        .then(() => {
            cy.get('[data-test=artivact-notify-success]')
                .should('be.visible')
                .find('button')
                .click({multiple: true})
        })
})

Cypress.Commands.add('login', (language) => {
    cy.get('[data-test=login-button]')
        .should('be.visible')
        .click()
    cy.get('[data-test=username-input]')
        .should('be.visible')
        .type('admin')
    cy.get('[data-test=password-input]')
        .should('be.visible')
        .type('admin')
    cy.get('[data-test=submit-login-button]')
        .should('be.visible')
        .click()
    cy.get('[data-test=artivact-notify-success]')
        .should('be.visible')
        .find('button')
        .click()
})

Cypress.Commands.add('logout', (language) => {
    cy.get('[data-test=logout-button]')
        .should('be.visible')
        .click()
    cy.get('[data-test=login-button]')
        .should('be.visible')
})
