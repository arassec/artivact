import {clickButton} from '../../../../support/common-utils';

describe('User Manual -> Item Management -> Create Items', () => {

    Cypress.env("locales").forEach((locale) => {

        it('user-manual/item-management/assets/create-items/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            clickButton('item-settings-button')
            clickButton('import-items-button')

            cy.takeScreenshot('artivact-main-layout', locale, 'import')

            clickButton('item-settings-button')
            clickButton('create-item-button')

            cy.wait(['@itemDetailsPage'])

            cy.takeScreenshot('artivact-main-layout', locale, 'create')

        })

    })

})