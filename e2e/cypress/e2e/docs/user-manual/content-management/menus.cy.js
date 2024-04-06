import {createMenu, createMenuEntry, deleteMenu} from '../../../../support/menu-utils';

describe('User Manual -> Content Management -> Menus', () => {

    Cypress.env('locales').forEach((locale) => {

        it('user-manual/content-management/assets/menus/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            // Create menu and screenshots:
            createMenu(locale, 'Start', true)
            createMenu(locale, 'Middle', false)
            createMenu(locale, 'End', false)

            deleteMenu(locale, 'Middle', true)
            deleteMenu(locale, 'Start', false)
            deleteMenu(locale, 'End', false)

            // Menu with entries:
            createMenu(locale, 'Start', false)
            createMenuEntry(locale, 'Start', 'Alpha')
            createMenuEntry(locale, 'Start', 'Beta')
            createMenuEntry(locale, 'Start', 'Gamma')

            cy.get(`[data-test=menu-Start]`)
                .should('be.visible')
                .click()

            cy.get('[data-test=menu-entry-Beta]')
                .should('be.visible')
                .rightclick()

            cy.takeScreenshot(`menu-entry-context-menu-Beta`, locale)

            deleteMenu(locale, 'Start', false)
        })

    })

})