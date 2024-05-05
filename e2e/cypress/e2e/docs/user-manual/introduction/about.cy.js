describe('User Manual -> Introduction -> About', () => {

    Cypress.env("locales").forEach((locale) => {

        it('user-manual/introduction/assets/about/', () => {
            cy.switchLanguage(locale)
            cy.openArtivact()

            cy.takeScreenshot('artivact-main-layout', locale)

            cy.takeScreenshot('add-menu-button', locale)

            cy.takeScreenshot('locale-selection-button', locale)
            cy.takeScreenshot('item-settings-button', locale)
            cy.takeScreenshot('system-settings-button', locale)
            cy.takeScreenshot('account-settings-button', locale)
            cy.takeScreenshot('documentation-button', locale)

            cy.takeScreenshot('logout-button', locale)

            cy.logout();

            cy.takeScreenshot('login-button', locale)

            cy.login();
        })

    })

})