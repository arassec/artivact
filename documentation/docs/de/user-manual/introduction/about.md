# Artivact User Manual

## About

::: danger Work in progress
The user manual is currently in creation and will be updated with future releases.
:::

The Artivact user manual documents how to use the application to create and manage your virtual collections.

The desktop and web server variant of the application differ slightly in their feature sets.
If a function only applies to one of the installation variants, this will be denoted in the documentation by one of the
following badges:

- <Badge type="warning" text="server"/> - This functionality is only available, if Artivact is run in web server mode.
- <Badge type="warning" text="desktop"/> - This functionality is only available, if Artivact is started on your local computer.

## The Main Screen

After first start, the main screen is shown to the user.
At the top is the navigation and settings bar.

![artivact-main-layout](./assets/about/artivact-main-layout.png)

The following functions are available from left to right:

|                                 Button                                 | Description                                                                                                                     |
|:----------------------------------------------------------------------:|:--------------------------------------------------------------------------------------------------------------------------------|
|         ![add-menu-button](./assets/about/add-menu-button.png)         | Adds a new menu to the top navigation. See [Menu Management](/user-manual/content-management/menus) for details.                |
| ![locale-selection-button](./assets/about/locale-selection-button.png) | Selects the locale for internationalization (I18N). See TODO for details.                                                       |
|    ![item-setting-button](./assets/about/item-settings-button.png)     | Opens the items menu. From there you can create new items or import existing ones. See TODO for details.                        |
|      ![exhibitions-button](./assets/about/exhibitions-button.png)      | Opens the exhibitions configuration. From there you can create and manage exhibitions of your collection. See TODO for details. |
|  ![system-settings-button](./assets/about/system-settings-button.png)  | Opens the system settings menu. See TODO for details.                                                                           |
| ![account-settings-button](./assets/about/account-settings-button.png) | <Badge type="warning" text="server"/> Opens the account settings mennu. See TODO for details.                                   |
|    ![documentation-button](./assets/about/documentation-button.png)    | Opens this documentation from within the application.                                                                           |
|           ![logout-button](./assets/about/logout-button.png)           | <Badge type="warning" text="server"/> Logs the user out of the application.                                                     |
|            ![login-button](./assets/about/login-button.png)            | <Badge type="warning" text="server"/> Opens the login page.                                                                     |

Since there is no page currently defined, a default page is shown.

You can use the plus button to add a new menu entry and define a page as described in the next chapter.