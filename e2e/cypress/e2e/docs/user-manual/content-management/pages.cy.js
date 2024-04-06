import {createMenu, deleteMenu} from '../../../../support/menu-utils';

describe('User Manual -> Content Management -> Pages', () => {

    Cypress.env('locales').forEach((locale) => {

        it('user-manual/content-management/assets/pages/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            createMenu(locale, 'Start', false)

            cy.get('[data-test=menu-Start]')
                .should('be.visible')
                .rightclick()

            cy.get('[data-test=menu-add-page-button-Start]')
                .should('be.visible')
                .click()

            cy.takeScreenshot('artivact-main-layout', locale)

            cy.takeScreenshot('edit-page-button', locale)

            cy.get('[data-test=edit-page-button]')
                .should('be.visible')
                .click()

            cy.takeScreenshot('artivact-main-layout', locale, 'editmode')

            cy.takeScreenshot('close-button', locale)
            cy.takeScreenshot('index-page-button', locale)
            cy.takeScreenshot('add-widget-button', locale)
            cy.takeScreenshot('save-button', locale)

            cy.get('[data-test=add-widget-button]')
                .should('be.visible')
                .click()

            cy.takeScreenshot('add-widget-modal', locale)

            cy.get('[data-test=add-widget-selection]')
                .first()
                .should('be.visible')
                .click()

            cy.get('[data-test=add-widget-selection-TEXT]')
                .should('be.visible')
                .click()

            cy.get('[data-test=add-widget-modal-approve]')
                .first()
                .should('be.visible')
                .click()

            cy.takeScreenshot('artivact-main-layout', locale, 'widget-added')

            cy.get('[data-test=text-widget-editor-heading]')
                .should('be.visible')
                .rightclick()

            cy.takeScreenshot('widget-context-menu', locale)

            cy.get('[data-test=widget-context-menu-edit-button]')
                .should('be.visible')
                .click()

            cy.takeScreenshot('widget-editor-modal', locale)

            cy.get('[data-test=widget-editor-modal-approve]')
                .should('be.visible')
                .click()
            deleteMenu(locale, 'Start', false)
        })

    })

})
