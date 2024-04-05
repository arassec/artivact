import './docs/user-manual/introduction/about.cy.js'
import './docs/user-manual/content-management/menus.cy.js'
import './docs/user-manual/content-management/pages.cy.js'

describe('All Documentation Tests', () => {

    before(() => {
        cy.openArtivact()

        cy.get('[data-test=artivact-system-settings]')
            .should('be.visible')
            .click()
            .get('[data-test=artivact-system-settings-appearance]')
            .should('be.visible')
            .click()
            .get('[data-test=appearance-config-locales]')
            .should('be.visible')
            .click()
            .get('[data-test=appearance-config-locales-input]')
            .should('be.visible')
            .clear()
            .type('de')

        cy.get('[data-test=appearance-configuration-page-sage]')
            .click()
            .then(() => {
                cy.get('[data-test=artivact-notify-success]')
                    .should('be.visible')
            })
    })

})