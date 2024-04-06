import {clickButton} from '../../../../support/common-utils';

describe('User Manual -> Item Management -> Item Editing', () => {

    Cypress.env("locales").forEach((locale) => {

        it('user-manual/item-management/assets/item-editing/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            clickButton('item-settings-button')
            clickButton('create-item-button')

            cy.wait(['@itemDetailsPage'])

            cy.takeScreenshot('close-button', locale);
            cy.takeScreenshot('download-button', locale);
            cy.takeScreenshot('save-button', locale);

            cy.takeScreenshot('edit-item-tabs', locale);

            cy.wait(250)
            cy.takeScreenshot('edit-item-contents', locale, 'base')

            clickButton('edit-item-media-tab')
            cy.wait(250)
            cy.takeScreenshot('edit-item-contents', locale, 'media')
            cy.takeScreenshot('item-media-images-folder-button', locale)
            cy.takeScreenshot('item-media-images-upload-button', locale)

            clickButton('edit-item-properties-tab')
            cy.wait(250)
            cy.takeScreenshot('edit-item-contents', locale, 'properties')

            clickButton('edit-item-creation-tab')
            cy.wait(250)
            cy.takeScreenshot('edit-item-contents', locale, 'creation')
            cy.takeScreenshot('item-creation-camera-button', locale)
            cy.takeScreenshot('item-creation-create-model-button', locale)
        })

    })

})