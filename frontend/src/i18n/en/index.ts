export default {

  Common: {
    cancel: 'Cancel',
    save: 'Save',
    apply: 'Apply',
    username: 'Username',
    password: 'Password',
    passwordRepeat: 'Password (repeat)',
    email: 'E-Mail',
    messages: {
      creating: {
        success: '{item} created',
        failed: 'Creating \'{item}\' failed'
      },
      loading: {
        success: '{item} loaded',
        failed: 'Loading \'{item}\' failed',
      },
      saving: {
        success: '{item} saved',
        failed: 'Saving \'{item}\' failed'
      },
      deleting: {
        success: '{item} deleted',
        failed: 'Deleting \'{item}\' failed'
      }
    },
    items: {
      accounts: 'Accounts',
      account: 'Account',
      locales: 'Locales',
      page: 'Page',
      items: 'Items',
      item: 'Item',
      category: 'Category',
      properties: 'Properties',
      property: 'Property',
      tags: 'Tags',
      tag: 'Tag',
      exports: 'Exports',
      export: 'Export',
      menus: 'Menus',
      menu: 'Menu',
      applicationLocale: 'Application Locale',
      collectionExport: 'Collection Export',
      collectionExports: 'Collection Exports',
      configuration: {
        appearance: 'Appearance configuration',
        exchange: 'Exchange configuration',
        license: 'License configuration',
        peripherals: 'Peripherals configuration',
        properties: 'Properties configuration',
        tags: 'Tags configuration'
      }
    }
  },

  MainLayout: {
    login: 'Login',
    logout: 'Logout',
    messages: {
      userDataFailed: 'Loading user data failed!',
      logoutFailed: 'Logout failed!'
    },
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
    heading: 'Appearance Configuration'
  },

  BatchProcessingPage: {
    heading: 'Batch Processing',
    startButton: 'Start batch processing',
    parameters: {
      task: 'Task',
      taskDescription: 'Please select the task to perform on all selected items.',
      searchTerm: 'Item Selection',
      searchTermDescription: 'Specify a search term which will select all items that should be processed.',
      targetId: 'Target',
      targetIdDescription: 'Please select the target of the task to perform.',
    },
    dialog: {
      process: {
        heading: 'Batch Processing',
        description: 'Are you sure you want to start batch processing the selected items?',
        approve: 'Start batch processing'
      }
    },
    messages: {
      process: {
        success: 'Batch processing finished',
        failed: 'Batch processing failed!'
      }
    }
  },

  CollectionExportsPage: {
    heading: 'Collection Exports',
    tabs: {
      configuration: 'Configuration',
      import: 'Import'
    },
    configuration: {
      heading: 'Export Configurations',
      description: 'This page can be used to create and manage collection exports. Those can be used to transfer menus, their pages and items displayed on the pages to other Artivact instances. Additionally, the collection exports can be served by Artivact instances to be used e.g. in the Artivact XR app.',
    },
    import: {
      heading: 'Collection Import',
      description: 'Here you can import previously created collection exports. Those can either be imported with all menus, pages and items or as packaged export for distribution only.',
      completeImport: 'Complete Import',
      forDistributionImport: 'Import for Distribution',
      exportFile: 'Collection Export'
    },
    dialog: {
      delete: {
        heading: 'Delete Export Configuration?',
        description: 'Do you want to delete this collection export configuration and all associated files? This action cannot be undone!',
        approve: 'Delete Configuration'
      },
      build: {
        heading: 'Create Export File?',
        description: 'Do you want to create the export file for this configuration now? This may take some time, depending on the collection size.',
        approve: 'Create Export File'
      }
    },
    messages: {
      buildExportFileFailed: 'Export file building failed!',
      importSuccess: 'Import successful',
    }
  },

  EditablePage: {
    dialog: {
      heading: 'Unsaved Changes!',
      content: 'There are unsaved changes to the page configuration. Do you really want to leave?',
      approve: 'Leave'
    }
  },

  ErrorNotFoundPage: {
    heading: '404',
    description: 'Page not found...'
  },

  ExchangeConfigurationPage: {
    heading: 'Data Exchange',
    configuration: {
      description: 'Here you can configure exchange parameters for syncing items with remote Artivact instances.',
    }
  },

  ItemDetailsPage: {
    button: {
      tooltip: {
        delete: 'Delete item',
        download: 'Download item data as ZIP',
        sync: 'Synchronize item with remote server',
        edit: 'Edit item',
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
      },
      unsavedChanges: {
        heading: 'Unsaved Changes!',
        content: 'There are unsaved changes to the item configuration. Do you really want to leave?',
        approve: 'Leave'
      }
    },
    editor: {
      title: 'Title',
      description: 'Description'
    }
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
    heading: 'Properties',
    tabs: {
      configuration: 'Configuration',
      export: 'Export',
      import: 'Import',
    },
    configuration: {
      heading: 'Configuration',
      description: 'Here the properties of items can be configured. They are organized in categories, which can be ordered by drag & drop. The categories, with their respective properties, are shown at the bottom of the item details page.',
      noPropertiesDefined: 'There are currently no properties defined.',
    },
    export: {
      heading: 'Properties Export',
      description: 'You can export the current properties configuration with the button below. A JSON file containing the current configuration will be created.',
      button: 'Export Properties'
    },
    import: {
      heading: 'Properties Import',
      description: 'You can upload a previously created properties export here and OVERWRITE the current properties with it.',
      button: 'Import Properties',
    },
    messages: {
      uploadSuccess: 'Properties configuration uploaded'
    }
  },

  SearchConfigurationPage: {
    heading: 'Search Engine Configuration',
    label: 'Here you can (re-)create the search index if this might be necessary for some reason.',
    btnLabel: '(Re-)Create Search Index',
    messages: {
      indexCreated: 'Index created successfully.',
      indexCreationFailed: 'Index creation failed!'
    }
  },

  TagsConfigurationPage: {
    heading: 'Tags',
    configuration: {
      heading: 'Configuration',
      description: 'Tags can be used to categorize items beyond their properties. They should be considered meta-data and not be used to replace properties.',
      noTagsDefined: 'There are currently no tags defined.',
    },
    export: {
      heading: 'Export',
      description: 'You can export the current tags configuration with the button below. A JSON file containing the current configuration will be created.',
      button: 'Export Tags',
    },
    import: {
      heading: 'Import',
      description: 'You can upload a previously created tags export here and OVERWRITE the current tags with it.',
      button: 'Import Tags',
    },
    messages: {
      uploadSuccess: 'Tags configuration uploaded'
    }
  },

  ArtivactAppearanceConfigurationEditor: {
    description: 'Configures the appearance of the Artivact application.',
    list: {
      title: {
        heading: 'Application Title',
        description: 'The title is displayed in the browser tab on every page.',
        label: 'Application Title'
      },
      locales: {
        heading: 'Supported Locales',
        description: 'Comma-separated list of locales supported by this installation. E.g. \'de,nl,ja\'. Those locales can be maintained in addition to the default locale by choosing it from the settings menu and editing translatable strings e.g. on pages.',
        label: 'Locales'
      },
      applicationLocale: {
        heading: 'Application Locale',
        description: 'Here you can select the locale used by the application, i.e. the language of all texts of the application, that are not edited by the user. By default, the system locale is used if it is supported by the application, or english if it isn\'t supported.',
        label: 'Locale',
        de: 'German',
        en: 'English',
        system: 'System language'
      },
      colors: {
        heading: 'Color Theme',
        description: 'The color theme can be customized with the following settings.',
      },
      favicon: {
        heading: 'Favicon',
        descriptionPrefix: 'A custom Favicon can be uploaded here. The favicon file must be in ICO (.ico) format!',
        description: 'The uploaded file is directly installed as favicon!',
        descriptionSuffix: 'Due to browser caching the newly uploaded favicon might not directly be visible in your browser tab.',
        saved: 'Favicon saved'
      },
      license: {
        heading: 'Media Files License',
        description: 'Configures the license string displayed on the artivacts details-page. The license is composed of a prefix, the actual license and a suffix. If an URL is configured, the license will be rendered as link to the provided URL.',
        descriptionSuffix: 'The final license text might look like: "Media files are provided under \'CC BY-SA\' license."',
        card: {
          heading: 'License',
          prefixDescription: 'The first part of the license text, e.g. \'Media files are provided under\'.',
          prefixLabel: 'Prefix',
          licenseDescription: 'The name of the license, e.g. \'MIT\' or \'CC BY-SA\'.',
          licenseLabel: 'License',
          suffixDescription: 'The last part of the license text, e.g. \'license\'.',
          suffixLabel: 'Suffix',
          urlDescription: 'If provided, this URL will be used to make the license text into a link.',
          urlLabel: 'License-URL'
        }
      },
    }
  },

  ArtivactCollectionExportEditor: {
    dialog: {
      create: {
        heading: 'Create Export Configuration',
        approve: 'Create Configuration'
      },
      edit: {
        heading: 'Edit Export Configuration',
        approve: 'Save Configuration'
      }
    },
    label: {
      sourceId: 'Source of collection export',
      title: 'Title',
      description: 'Description',
      optimizeSize: 'Optimize export size',
      applyRestrictions: 'Exclude restricted elements',
      exportFileMissing: 'No export file exists for this configuration! You can create one by clicking on the respective button above.',
      lastModified: 'Last modification of export file: ',
      coverPicture: 'Cover Picture',
      noCoverPicture: 'No cover picture available.',
    },
    button: {
      create: 'Create Export Configuration'
    },
    tooltip: {
      delete: 'Delete this export configuration and associated files',
      edit: 'Edit this export configuration',
      build: 'Re-create the export file for this collection export configuration',
      buildNew: 'Create a new export file for this collection export configuration',
      download: 'Download the export file',
      deleteCoverPicture: 'Delete cover picture from collection export',
      restricted: 'Access to this collection export is restricted',
      distributionOnly: 'This collection export is for distribution only',
    }
  },

  ArtivactContentExportConfigurationEditor: {
    tooltip: {
      deleteContentExport: 'Delete Export'
    },
    label: {
      lastModified: 'Last modified: '
    }
  },

  ArtivactExchangeConfigurationEditor: {
    synchronization: {
      heading: 'Synchronization Configuration',
      server: {
        description: 'URL of the remote Artivact server you want to use to synchronize items.',
        label: 'Remote Artivact Server'
      },
      token: {
        description: 'API token to use for synchronization. The token configured here must also be configured for a user account on the remote server. This user will be used on the remote side for authentication and authorization.',
        label: 'API Token'
      }
    }
  },

  ArtivactModelEditor: {
    tooltip: {
      open: 'Open the directory containing the 3D model.',
      upload: 'Upload an existing GLTF/GLB 3D model to this item.',
      delete: 'Remove model from item',
      move: 'Change sort order per Drag&Drop.'
    },
    dialog: {
      upload: {
        heading: 'Upload 3D model',
        label: 'Add model'
      },
      delete: {
        heading: 'Remove 3D model?',
        description: 'Are you sure you want to delete this model? This action cannot be undone!',
        approve: 'Delete model'
      },
      messages: {
        openFailed: 'Could not open model directory!'
      }
    }
  },

  ArtivactMenuBar: {
    tooltip: {
      edit: 'Right click to edit menu'
    },
    label: {
      menu: 'Menu',
      edit: 'Edit Menu',
      delete: 'Delete Menu',
      export: 'Export Menu',
      left: 'Move Left',
      right: 'Move Right',
      editEntry: 'Edit Menu Entry',
      deleteEntry: 'Delete Menu Entry',
      exportEntry: 'Export Menu Entry',
      up: 'Move Up',
      down: 'Move Down',
      createMenu: 'Create Menu',
      importMenu: 'Import Menu',
      add: 'Add Page',
      addEntry: 'Add Entry',
      exportConfiguration: {
        optimizeSize: 'Optimize export size',
        applyRestrictions: 'Exclude restricted elements',
        excludeItems: 'Exclude items from export'
      }
    },
    dialog: {
      add: 'Add Menu',
      edit: 'Edit Menu',
      addEntry: 'Add Menu Entry',
      editEntry: 'Edit Menu Entry',
      description: 'Enter the menu\'s name.',
      descriptionEntry: 'Enter the menu entry\'s name.',
      delete: 'Delete Menu?',
      deleteEntry: 'Delete Menu Entry?',
      deleteDescription: 'Are you sure you want to delete this menu including its page and menu entries? This action cannot be undone!',
      deleteApprove: 'Delete Menu',
      deleteApproveEntry: 'Delete Menu Entry',
      exportConfiguration: 'Export Configuration',
      exportApprove: 'Export Content',
      import: 'Import Menu',
      importDescription: 'Please select a previously exported menu and upload the export file here.',
      importFileUpload: 'Export File Upload'
    },
    messages: {
      movingFailed: 'Moving failed',
      exportFinished: 'Export finished'
    }
  },

  ArtivactOperationInProgressDialog: {
    heading: 'Operation in Progress',
    failedHeading: 'Operation Failed',
    details: 'Details'
  },

  ArtivactPage: {
    tooltip: {
      edit: 'Edit page',
      cancel: 'Leave Edit Mode',
      add: 'Add Widget',
      indexPageYes: 'This IS the index page',
      indexPageNo: 'This is NOT the index page',
    },
    label: {
      noIndexPage: 'No index page has been defined yet. Create a menu, add a page to it and edit it to be the index page.',
      addWidget: 'Add Widget',
      pageTitle: 'Page Title',
      textTitle: 'Text Title',
      textContent: 'Text Content',
      text: 'Text',
      infoBoxTitle: 'Info-Box Title',
      infoBoxContent: 'Info-Box Content',
      avatarSubtext: 'Avatar Subtext'
    },
    dialog: {
      heading: 'Widget Selection',
      description: 'WARNING: Adding a new widget will save the current page configuration!',
      type: 'Widget Type'
    }
  },

  ArtivactPeripheralsConfigurationEditor: {
    description: 'Configures the peripherals for 3D model creation of the Artivact application. Fallback-Options can be used if the peripheral or external software can not be used at all.',
    turntable: {
      heading: 'Turntable Configuration',
      description: 'Automatic rotation of captured items via turntables can be configured here. Currently only the open source Artivact turntable is supported. If you use a turntable manually you can configure the fallback option and set a delay to give you time to rotate the turntable by hand.',
      label: 'Turntable to use',
      delay: 'Turntable delay in milliseconds'
    },
    camera: {
      heading: 'Camera Configuration',
      description: 'Images are captured with third party applications, which can be configured here. On Windows, DigiCamControl is supported. On Linux, gphoto2 must be used.',
      label: 'Photo-Capture Software to use',
      digiCamControlExe: 'DigiCamControl Executable',
      digiCamControlUrl: 'DigiCamControl Webserver URL',
      gphotoExe: 'gphoto2 Executable'
    },
    background: {
      heading: 'Background Removal',
      description: 'Automatic background removal of captured images is implemented using the open source tool \'rembg\' by Daniel Gatis (https://github.com/danielgatis/rembg). You can e.g. provide it with docker by running',
      dockerCmd: 'docker run -d -p 7000:7000 --name=rembg --restart=always danielgatis/rembg s',
      label: 'Background-Removal Software to use',
      rembg: 'rembg Webserver URL'
    },
    creator: {
      heading: '3D Model-Creator',
      description: 'For 3D model creation currently "Metashape", "Meshroom" and "RealityCapture" are supported.',
      label: 'Photogrammetry Software to use',
      meshroom: 'Meshroom Executable',
      metashape: 'Metashape Executable',
      RealityCapture: 'RealityCapture Executable. Append "#headless#12345" to run in headless mode and limit faces to 12345.'
    },
    editor: {
      heading: '3D Model-Editor',
      description: 'For editing created 3D models, Blender3D can be configured here.',
      label: '3D Model Editor to use',
      blender: 'Blender3D Executable'
    }
  },

  ArtivactPropertiesConfigurationEditor: {
    tooltip: {
      delete: 'Delete Category',
      deleteProperty: 'Delete Property',
      switchCategory: 'Move property to a different category',
      up: 'Move property up',
      down: 'Move property down'
    },
    button: {
      addProperty: 'Add Property',
      addCategory: 'Add Category'
    },
    newCategory: 'New Category',
    newProperty: 'New Property'
  },

  ArtivactPropertyValueRangeEditor: {
    label: 'Range of values:',
    tooltip: {
      edit: 'Edit value',
      delete: 'Delete value',
      add: 'Add value'
    },
    dialog: {
      heading: 'Configure Value',
      label: 'Value'
    },
    newValue: 'New Value'
  },

  ArtivactRestrictedTranslatableItemEditor: {
    default: 'Default: ',
    tooltip: {
      more: 'Show details',
      less: 'Hide details'
    }
  },

  ArtivactRestrictionsEditor: {
    restrictions: 'Restrictions:'
  },

  ArtivactSettingsBar: {
    tooltip: {
      locales: 'Locale Selection',
      systemSettings: 'System Settings',
      account: 'Account Settings',
      documentation: 'Open the documentation'
    },
    default: 'Default',
    itemSettings: 'Item Settings',
    createItem: 'Create Item',
    importItem: 'Import Item',
    license: 'License',
    appearance: 'Appearance',
    peripherals: 'Peripherals',
    exchange: 'Exchange',
    locale: 'Locale',
    exhib: 'Exhib.',
    system: 'System',
    search: 'Searchengine',
    documentation: 'Docs',
    batch: 'Batch Processing',
    dialog: {
      import: 'Import Item',
      importDescription: 'Import a previously exported item. Please select the export file and upload it here.',
      importFileUpload: 'Export File Upload'
    }
  },

  ArtivactTagsConfigurationEditor: {
    tooltip: {
      delete: 'Delete Tag',
    },
    url: 'URL',
    addTag: 'Add Tag',
    newTag: 'New Tag'
  },

  ItemImageEditor: {
    tooltip: {
      open: 'Open the directory containing the images.',
      upload: 'Upload an existing JPG/PNG image to this item.',
      delete: 'Remove image from item',
      move: 'Change sort order per Drag&Drop.'
    },
    dialog: {
      upload: {
        heading: 'Upload image',
        label: 'Add image'
      },
      delete: {
        heading: 'Delete Image?',
        description: 'Are you sure you want to delete this image? This action cannot be undone!',
        approve: 'Delete image'
      },
      messages: {
        openFailed: 'Could not open model directory!'
      }
    }
  },

  ItemImageSetEditor: {
    tooltip: {
      capture: 'Capture photos into new image set',
      open: 'Open directory containing images',
      upload: 'Upload existing images into new image set',
      details: 'Show image set details',
      backgrounds: 'Remove image backgrounds',
      delete: 'Delete image set',
    },
    label: {
      numPhotos: 'Number of photos',
      turntable: 'Use Turntable?',
      delay: 'Turntable Delay',
      backgrounds: 'Remove image backgrounds?'
    },
    captureParameters: 'Photo-Capture Parameters',
    startCapturing: 'Start Capturing',
    dialog: {
      upload: {
        heading: 'Upload Files to new Image-Set',
        label: 'Add Images'
      },
      details: {
        heading: 'Image-Set Details',
        transfer: 'Transfer image to item media',
        deleteImage: 'Delete image'
      },
      delete: {
        heading: 'Delete Image-Set?',
        description: 'Are you sure you want to delete this Image-Set and all its files? This action cannot be undone!',
        approve: 'Delete Image-Set'
      }
    },
    messages: {
      capturingFailed: 'Capturing photos failed!',
      backgroundFailed: 'Background removal failed!',
      imageSetFailed: 'Image-Set creation failed!',
      openingFailed: 'Opening directory failed!',
      transferred: 'Image transferred',
      transferFailed: 'Image transfer failed!',
      imageSetDeleted: 'Image-Set deleted',
      imageSetDeletionFailed: 'Image-Set deletion failed!',
      operationSuccess: 'Item saved',
      operationFailed: 'Operation failed!'
    }
  },

  ItemMediaCarousel: {
    tooltip: {
      image: 'Show images',
      model: 'Show 3D models',
    }
  },

  ItemModelSetEditor: {
    tooltip: {
      create: 'Create model from image sets',
      open: 'Open directory containing model directories',
      details: 'Show model files',
      openModel: 'Open directory containing model files',
      edit: 'Edit 3D model in editor',
      delete: 'Delete 3D model'
    },
    dialog: {
      details: {
        heading: 'Model-Set Details',
        transfer: 'Transfer 3D model to item media'
      },
      delete: {
        heading: 'Delete Model-Set?',
        description: 'Are you sure you want to delete this Model-Set and all its files? This action cannot be undone!',
        approve: 'Delete Model-Set'
      }
    },
    messages: {
      loadingFailed: 'Loading Model-Set failed!',
      creationFailed: 'Creating Model-Set failed!',
      openFailed: 'Opening directory failed!',
      editingFailed: 'Editing 3D model failed!',
      transferSuccess: 'Model transferred',
      transferFailed: 'Model transfer failed!',
      deleteSuccess: 'Model-Set deleted',
      deleteFailed: 'Model-Set deletion failed!',
      operationFailed: 'Operation failed'
    }
  },

  ItemModelViewer: {
    downloadModelButtonLabel: 'Download Model',
  },

  ArtivactItemSearchInput: {
    error: 'Please enter a search query.',
    label: {
      maxResults: 'Max Results',
      term: 'Search Term',
      fulltext: 'Syntax for searching property values in fulltext search: "PROPERTY_ID=[SEARCH_TERM]"',
      property: 'Syntax for searching property values only: PROPERTY_ID:"[SEARCH_TERM]"',
      addTag: 'Add Tag',
      addProperty: 'Add Property'
    }
  },

  Widget: {
    label: {
      navigationTitle: 'Navigation'
    }
  },

  AvatarWidget: {
    label: {
      image: 'Avatar Image',
      subtext: 'Avatar Subtext'
    }
  },

  ImageTextWidget: {
    label: {
      image: 'Image',
      text: 'Text',
      fullscreenAllowed: 'Configures whether the image can be opened in the fullscreen detail view.',
    }
  },

  InfoBoxWidget: {
    label: {
      heading: 'Heading',
      content: 'Content',
      outlined: 'Outlined'
    }
  },

  PageTitleWidget: {
    label: {
      bgImage: 'Background Image',
      title: 'Title'
    }
  },

  ItemSearchWidget: {
    label: {
      noSearchResults: 'No search results available!',
      heading: 'Heading',
      content: 'Content',
      pageSize: 'Page size',
    },
    messages: {
      noSearchResults: 'No search results found!',
      searchFailed: 'Search failed!',
    }
  },

  SpaceWidget: {
    label: {
      spaceUnits: 'Space Units'
    }
  },

  TextWidget: {
    label: {
      heading: 'Heading',
      content: 'Content'
    }
  },

  WidgetTemplate: {
    tooltip: {
      edit: 'Right click to edit widget'
    },
    label: {
      edit: 'Edit Widget',
      close: 'Close Widget Editor',
      moveUp: 'Move Widget Up',
      moveDown: 'Move Widget Down',
      delete: 'Delete Widget',
      addAbove: 'Add Widget Above',
      addBelow: 'Add Widget Below',
    }
  },

  Progress: {
    MediaCreationService: {
      captureStart: 'Start capturing photos.',
      captureInProgress: 'Capturing photos...',
      captureFailed: 'Capturing photos failed!',
      rembgStart: 'Start removing backgrounds from images.',
      rembgInProgress: 'Removing backgrounds from images...',
      rembgFailed: 'Removing backgrounds from images failed!',
      imageSetStart: 'Start importing images to new image-set.',
      imageSetInProgress: 'Adding images to image-set...',
      imageSetFailed: 'Error during image-set creation!',
      createModelStart: 'Creating model in external editor.',
      createModelFailed: 'Error during model creation!',
      editModelStart: 'Editing model in external editor.',
      editModelFailed: 'Error during model editing!',
      copyImages: 'Copying images...'
    },
    SearchService: {
      createIndex: '(Re-)Creating search index...',
      createIndexFailed: '(Re-)Creation of search index failed!',
    },
    ItemService: {
      uploading: 'Uploading file to remote Artivact server.',
      uploadFailed: 'Could not upload item file to remote server!',
    },
    ImportService: {
      importContent: 'Importing content...',
      importContentFailed: 'Import failed!'
    },
    BatchService: {
      process: 'Batch processing items...',
      processingFailed: 'Processing items failed!'
    },
    CollectionExportService: {
      buildCollectionExport: 'Building collection export file...',
      buildCollectionExportFailed: 'Building export file failed!',
      importCollection: 'Importing collection...'
    }
  },

  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'User',

  PAGE_TITLE: 'Page Title',
  PAGE_TITLE_DESCRIPTION: 'Page title with hero image and heading.',
  TEXT: 'Text',
  TEXT_DESCRIPTION: 'Text block with optional heading. Supports Markdown for text formatting.',
  IMAGE_TEXT: 'Image and Text',
  IMAGE_TEXT_DESCRIPTION: 'An image next to a text block. Supports Markdown for text formatting.',
  ITEM_SEARCH: 'Item Search',
  ITEM_SEARCH_DESCRIPTION: 'Either presents the results of a predefined item search, or lets the user input search parameters and displays the results.',
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
  REALITY_CAPTURE_MODEL_CREATOR_ADAPTER: 'RealityCapture',
  FALLBACK_MODEL_EDITOR_ADAPTER: 'Fallback',
  BLENDER_MODEL_EDITOR_ADAPTER: 'Blender3D',

  DELETE_ITEM: 'Delete item',
  ADD_TAG_TO_ITEM: 'Add tag',
  REMOVE_TAG_FROM_ITEM: 'Remove tag',
  UPLOAD_MODIFIED_ITEM: 'Upload modified item'

};
