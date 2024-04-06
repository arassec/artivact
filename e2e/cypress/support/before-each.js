before(() => {
    cy.openArtivact()

    cy.get('[data-test=system-settings-button]')
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

    cy.get('[data-test=appearance-configuration-page-save]')
        .click({multiple: true})
        .then(() => {
            cy.get('[data-test=artivact-notify-success]')
                .should('be.visible')
        })
});

beforeEach(() => {
    cy.intercept('GET', '/api/item/**').as('itemDetailsPage')
});
