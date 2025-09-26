# Pages

## Page Mode

After adding a page to a menu or menu entry, it can be opened by clicking on the menu (or menu entry).

Newly created pages are empty.

By clicking on the edit button in the top right, which is only shown to administrators and users, you can switch to page
edit mode:
![edit-page-button](../item-management/assets/item-details-page/edit-item-button.png)

Changes to the page are saved automatically.

By clicking on the exit button, you leave page edit mode again:
![close-page-edit-mode-button](../item-management/assets/item-editor/close-item-editor-button.png)

## Edit Mode

### Page Publishing <Badge type="warning" text="server"/>

When run in server mode, artivact won't edit pages directly. In order to modify
a page without publishing it to users immediately, the work-in-progress is saved until published by the user.
Thus, in edit mode, new buttons are shown at the top of the page, which offer the following functionality:

|                              Button                              | Description                                                                                 |
|:----------------------------------------------------------------:|:--------------------------------------------------------------------------------------------|
|     ![reset-wip-button](./assets/pages/reset-wip-button.png)     | Reverts all changes and resets the page to the currently published one.                     |
|   ![publish-wip-button](./assets/pages/publish-wip-button.png)   | Publishes the current work-in-progress to the public.                                       |
| ![edit-metadata-button](./assets/pages/edit-metadata-button.png) | Opens a dialog to edit page metadata which can be used e.g. for search engine optimization. |

### Adding Widgets

Widgets are added by clicking the respective button, either on top of the page if no widgets are defined or below an
existing widget. A dialog opens and provides a selection of available widgets.

See [Widgets](./widgets) for an overview of available widgets.

### Editing Widgets

After adding a widget, it is displayed on the page with its editor opened directly.

For existing widgets, the editor can be opened by hovering a widget with the mouse and clicking on it.

Widgets can be sorted on the page with drag and drop using the drag-indicator button on the top left of each widget.
Below that is a button to delete a widget from the page.