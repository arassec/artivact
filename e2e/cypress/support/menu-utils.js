export function createMenu(locale, menu, takeScreenshot) {
    cy.get('[data-test=add-menu-button]')
        .should('be.visible')
        .click()

    cy.get('[data-test=add-menu-modal-menu-name]')
        .first() // desktop resolution
        .should('be.visible')
        .type(menu)

    if (takeScreenshot) {
        cy.takeScreenshot('add-menu-modal', locale)
    }

    cy.get('[data-test=add-menu-modal-approve-button]')
        .first()
        .should('be.visible')
        .click()

    if (takeScreenshot) {
        cy.takeScreenshot('artivact-menu-bar', locale)
    }
}

export function deleteMenu(locale, menu, takeScreenshot) {
    cy.get(`[data-test=menu-${menu}]`)
        .should('be.visible')
        .rightclick()

    if (takeScreenshot) {
        cy.takeScreenshot(`menu-context-menu-${menu}`, locale)
    }

    cy.get(`[data-test=menu-delete-button-${menu}]`)
        .should('be.visible')
        .click()

    cy.get('[data-test=delete-menu-approve-button]')
        .first()
        .should('be.visible')
        .click()
}

export function createMenuEntry(locale, menu, entry) {
    cy.get(`[data-test=menu-${menu}]`)
        .should('be.visible')
        .rightclick()

    cy.get(`[data-test=menu-add-entry-button-${menu}]`)
        .should('be.visible')
        .click()

    cy.get('[data-test=add-menu-modal-menu-name]')
        .first() // desktop resolution
        .should('be.visible')
        .type(entry)

    cy.get('[data-test=add-menu-modal-approve-button]')
        .first()
        .should('be.visible')
        .click()
}
