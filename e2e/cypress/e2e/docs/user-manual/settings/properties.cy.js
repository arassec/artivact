import {clickButton} from '../../../../support/common-utils';

describe('User Manual -> Settings -> Properties', () => {

    Cypress.env("locales").forEach((locale) => {

        it('user-manual/settings/assets/properties/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            clickButton('system-settings-button')
            clickButton('artivact-system-settings-properties')

            cy.wait(['@propertiesSettingsPage'])

            clickButton('add-category-button')

            cy.get('[data-test=category-expansion-item]')
                .first()
                .should('be.visible')
                .click()

            cy.get('[data-test=category-name-input]')
                .first()
                .should('be.visible')
                .clear()
                .type('Alpha')

            clickButton('add-property-button')

            cy.get('[data-test=property-name-input]')
                .first()
                .should('be.visible')
                .clear()
                .type('Alpha')

        })

    })

})