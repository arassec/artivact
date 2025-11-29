# Application Appearance

## Configuration

The following aspects of the application's appearance can be configured on this page:

- Application Title
    - The title of the application, displayed in the browser tab or the application window.
- Index Page
    - The page that should be loaded by default if no page ID is specified in the URL.
- Supported Locales
    - Comma-separated list of language codes, e.g. ``de,nl,ja``.
    - The locale will be selected based on the user's system settings (<Badge type="warning" text="desktop"/>) or
      browser preferences (<Badge type="warning" text="server"/>)
    - The ``default`` locale is used, if none of the configured locales match the user's preferences.
- Application Locale  <Badge type="warning" text="desktop"/>
    - Locale of Artivact itself, i.e. the UI elements and messages etc.
    - Currently supported are english and german.
- Color Theme
    - Artivact's color theme can here be customized.
- Favicon <Badge type="warning" text="server"/>
    - A custom favicon can be uploaded here, which will be used in the browser tab.
- Media File License
    - Offers the option to configure a license term which is displayed on the item's details page below the media files.
    - Can be used to restrict the item's media files to a certain license.