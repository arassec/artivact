export default {

  Common: {
    cancel: 'Cancel',
    save: 'Save',
    username: 'Username',
    password: 'Password',
    passwordRepeat: 'Password (repeat)',
    email: 'E-Mail',
    messages: {
      creating: {
        success: '{item} created',
        failed: 'Creating {item} failed'
      },
      loading: {
        success: '{item} loaded',
        failed: 'Loading {item} failed',
      },
      saving: {
        success: '{item} saved',
        failed: 'Saving {item} failed'
      },
      deleting: {
        success: '{item} deleted',
        failed: 'Deleting {item} failed'
      }
    },
    items: {
      accounts: 'Accounts',
      account: 'Account',
      locales: 'Locales',
      page: 'Page',
      item: 'Item',
      properties: 'Properties',
      tag: 'Tag',
      configuration: {
        appearance: 'Appearance configuration',
        exchange: 'Exchange configuration',
        exhibitions: 'Exhibitions configuration',
        license: 'License configuration',
        peripherals: 'Peripherals configuration',
        properties: 'Properties configuration',
        tags: 'Tags configuration'
      }
    }
  },

  MainLayout: {
    login: 'Login',
    logout: 'Logout'
  },

  AccountsConfigurationPage: {
    heading: 'Accounts Administration',
    description: 'All system accounts can be configured on this page.',
    button: {
      addAccount: 'Add Account'
    },
    card: {
      heading: 'Accounts',
      button: {
        tooltip: {
          delete: 'Delete account',
          edit: 'Edit account'
        },
      }
    },
    dialog: {
      create: {
        heading: 'Create Account'
      },
      edit: {
        heading: 'Edit Account'
      },
      delete: {
        heading: 'Delete Account?',
        description: 'Are you sure you want to delete this account? This action cannot be undone!',
        button: 'Delete Account'
      },
      user: 'Has \'User\' rights?',
      admin: 'Has \'Administrator\' rights?'
    }
  },

  AccountSettingsPage: {
    heading: 'Account Settings',
    description: 'Here you can configure your account settings.',
    card: {
      heading: 'Account'
    },
    apiToken: 'API Token'
  },

  AppearanceConfigurationPage: {
    heading: 'Appearance Configuration',
    messages: {
      loadingLocalesFailed: 'Loading locales failed'
    }
  },

  ErrorNotFoundPage: {
    heading: '404',
    description: 'Page not found...'
  },

  ExchangeConfigurationPage: {
    heading: 'Exchange Configuration',
    description: 'On this page you can configure exchange parameters.'
  },

  ExhibitionsConfigurationPage: {
    heading: 'Artivact Exhibitions'
  },

  ItemDetailsPage: {
    button: {
      tooltip: {
        delete: 'Delete item',
        sync: 'Synchronize item with remote server',
        edit: 'Edit item'
      },
    },
    dialog: {
      delete: {
        heading: 'Delete Item?',
        description: 'Are you sure you want to delete this item and all its files? This action cannot be undone!',
        button: 'Delete Item'
      }
    },
    messages: {
      sync: {
        success: 'Item synchronized',
        failed: 'Synchronization failed'
      }
    }
  },

  ItemEditPage: {
    button: {
      tooltip: {
        close: 'Leave edit mode and return to item page',
        download: 'Download item data as ZIP',
        save: 'Save item',
        addTag: 'Add tag to item',
        removeTag: 'Remove tag from item',
        deleteModel: 'Deletes the model from the item'
      }
    },
    tab: {
      base: 'Base Data',
      media: 'Media',
      properties: 'Properties',
      creation: 'Creation'
    },
    label: {
      tags: 'Tags:',
      images: 'Images',
      models: '3D Models',
      noProperties: 'There are currently no property definitions for items available. Go to the configuration page and add properties.'
    },
    dialog: {
      addTag: {
        heading: 'Add Tag'
      }
    },
    editor: {
      title: 'Title',
      description: 'Description'
    }
  },

  ItemImportPage: {
    heading: 'Item Import',
    description: {
      scan: 'Scans the data directory for new or updated items.',
      upload: 'Imports a previously exported item.'
    },
    button: {
      scan: 'Scan',
      upload: 'Import Item'
    },
    messages: {
      scanSuccessful: 'Scan successful',
      scanFailed: 'Scan failed',
      itemUploaded: 'Item uploaded'
    }
  },

  LicenseConfigurationPage: {
    heading: 'License Configuration',
  },

  LoginPage: {
    login: 'Login',
    messages: {
      loginSuccessful: 'Login successful',
      loginFailed: 'Login failed',
      loadingUserdataFailed: 'Loading user data failed',
      loadingMenusFailed: 'Loading menus failed'
    }
  },

  PeripheralsConfigurationPage: {
    heading: 'Peripherals Configuration'
  },

  PropertiesConfigurationPage: {
    heading: 'Properties Configuration',
    description: 'Here the properties of items can be configured. They are organized in categories, which can be ordered by drag & drop. The categories, with their respective properties, are shown at the bottom of the item details page.',
    noPropertiesDefined: 'There are currently no properties defined.',
    importexport: {
      heading: 'Properties Import/Export',
      export: 'You can export the current properties configuration with the button below. A JSON file containing the current configuration will be created.',
      import: 'You can upload a previously created properties export here and OVERWRITE the current properties with it.',
      button: {
        export: 'Export Properties',
        import: 'Import Properties'
      },
    },
    messages: {
      uploadSuccess: 'Properties configuration uploaded'
    }
  },

  TagsConfigurationPage: {
    heading: 'Tags Configuration',
    description: 'Tags can be used to categorize items beyond their properties. They should be considered meta-data and not be used to replace properties.',
    noTagsDefined: 'There are currently no tags defined.',
    importexport: {
      heading: 'Tags Import/Export',
      export: 'You can export the current tags configuration with the button below. A JSON file containing the current configuration will be created.',
      import: 'You can upload a previously created tags export here and OVERWRITE the current tags with it.',
      button: {
        export: 'Export Tags',
        import: 'Import Tags'
      },
    },
    messages: {
      uploadSuccess: 'Tags configuration uploaded'
    }
  },

  ItemModelViewer_downloadModelButtonLabel: 'Download Model',

  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'User',

  widgetType: 'Widget Type',
  addWidgetWarn: 'WARNING: Adding a new widget will save the current page configuration!',
  PAGE_TITLE: 'Page Title',
  PAGE_TITLE_DESCRIPTION: 'Page title with hero image and heading.',
  TEXT: 'Text',
  TEXT_DESCRIPTION: 'Text block with optional heading. Supports Markdown for text formatting.',
  IMAGE_TEXT: 'Image and Text',
  IMAGE_TEXT_DESCRIPTION: 'An image next to a text block. Supports Markdown for text formatting.',
  ITEM_SEARCH: 'Item Search',
  ITEM_SEARCH_DESCRIPTION: 'Either presents the results of a predefined item search, or lets the user input search parameters and displays the results.',
  ITEM_CAROUSEL: 'Item Carousel',
  ITEM_CAROUSEL_DESCRIPTION: 'Shows three item cards at once with support for scrolling through all items of a predefined search.',
  INFO_BOX: 'Info Box',
  INFO_BOX_DESCRIPTION: 'A text box with optional heading that can be colored according to their three states: INFO, WARN and ALERT.',
  AVATAR: 'Avatar',
  AVATAR_DESCRIPTION: 'A portrait image with optional subtext and description to the right.',
  SPACE: 'Space',
  SPACE_DESCRIPTION: 'Adds empty space of configurable size to the page.',

  FALLBACK_BACKGROUND_REMOVAL_ADAPTER: 'Fallback',
  REMBG_REMOTE_BACKGROUND_REMOVAL_ADAPTER: 'rembg (Remote)',
  FALLBACK_CAMERA_ADAPTER: 'Fallback',
  DIGI_CAM_CONTROL_CAMERA_ADAPTER: 'DigiCamControl',
  DIGI_CAM_CONTROL_REMOTE_CAMERA_ADAPTER: 'DigiCamControl (Remote)',
  GPHOTO_TWO_CAMERA_ADAPTER: 'gphoto2',
  FALLBACK_TURNTABLE_ADAPTER: 'Fallback',
  ARTIVACT_TURNTABLE_ADAPTER: 'Artivact Turntable',
  FALLBACK_MODEL_CREATOR_ADAPTER: 'Fallback',
  MESHROOM_MODEL_CREATOR_ADAPTER: 'Meshroom',
  METASHAPE_MODEL_CREATOR_ADAPTER: 'Metashape',
  FALLBACK_MODEL_EDITOR_ADAPTER: 'Fallback',
  BLENDER_MODEL_EDITOR_ADAPTER: 'Blender3D',

};
