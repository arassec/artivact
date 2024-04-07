# Pages

## Page Mode

After adding a page to a menu or menu entry, it can be opened by clicking on the menu (or menu entry).

Newly created pages are empty.

![artivact-main-layout](./assets/pages/artivact-main-layout.png)

By clicking on the edit button in the top right, which is only shown to administrators and users, you can switch to page
edit mode:
![edit-page-button](./assets/pages/edit-page-button.png)

## Edit Mode

### General Options

In edit mode, new buttons are shown at the top of the page:

![artivact-main-layout-editmode](./assets/pages/artivact-main-layout-editmode.png)

The buttons offer the following functionality:

|                              Button                              | Description                                                                        |
|:----------------------------------------------------------------:|:-----------------------------------------------------------------------------------|
|      ![close-button](./assets/pages/close-button.png)      | Closes page edit mode without saving changes.                                      |
| ![index-page-button](./assets/pages/index-page-button.png) | Defines this page as index page, which is loaded if no specific page is requested. |
| ![add-widget-button](./assets/pages/add-widget-button.png) | Opens the dialog to add a widget to the page.                                      |
|       ![save-button](./assets/pages/save-button.png)       | Saves the current page configuration.                                              |

### Adding Widgets

Widgets are added by clicking the respective button. A dialog opens and provides a selection of available widgets:

![add-widget-modal](./assets/pages/add-widget-modal.png)

See [Widgets](./widgets) for an overview of available widgets.

### Editing Widgets

After adding a widget, it is displayed on the page:

![artivact-main-layout-widget-added](./assets/pages/artivact-main-layout-widget-added.png)

By hovering the widget with the mouse and right-clicking on it, a context menu to edit the widget is opened

![widget-context-menu](./assets/pages/widget-context-menu.png)

with the following options:

| Option           | Description                                                                                       |
|:-----------------|:--------------------------------------------------------------------------------------------------|
| Edit Widget      | Opens a modal to configure the widget.                                                            |
| Move Widget Up   | Moves the widget up one place.                                                                    |
| Move Widget Down | Moves the widget down one place.                                                                  |
| Delete Widget    | Deletes the widget directly (!) from the page. The widget is NOT deleted until the page is saved. |

The modal to configure a widget is split into to parts. The upper part is a preview of the results, the lower part
provides configuration options for the widget, depending on the type of the widget.

As an example, here is the text widget's modal:

![widget-editor-modal](./assets/pages/widget-editor-modal.png)

When clicking the ``Apply`` button, the changes are taken over to the page and the modal closes.