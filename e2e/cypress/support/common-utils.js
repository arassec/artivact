export function clickButton(selector) {
    cy.get('[data-test=' + selector + ']')
        .should('be.visible')
        .click()
}