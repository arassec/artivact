export default {
  Common: {
    cancel: 'Cancel',
    save: 'Save',
    apply: 'Apply',
    close: 'Close',
    username: 'Username',
    password: 'Password',
    passwordRepeat: 'Password (repeat)',
    email: 'E-Mail',
    messages: {
      creating: {
        success: '{item} created',
        failed: "Creating '{item}' failed",
      },
      loading: {
        success: '{item} loaded',
        failed: "Loading '{item}' failed",
      },
      saving: {
        success: '{item} saved',
        failed: "Saving '{item}' failed",
      },
      deleting: {
        success: '{item} deleted',
        failed: "Deleting '{item}' failed",
      },
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
      image: 'Image',
      images: 'Images',
      model: '3D Model',
      models: '3D Models',
      applicationLocale: 'Application Locale',
      collectionExport: 'Collection Export',
      collectionExports: 'Collection Exports',
      configuration: {
        appearance: 'Appearance configuration',
        exchange: 'Exchange configuration',
        license: 'License configuration',
        peripherals: 'Peripherals configuration',
        properties: 'Properties configuration',
        tags: 'Tags configuration',
      },
    },
  },

  MainLayout: {
    login: 'Login',
    logout: 'Logout',
    messages: {
      userDataFailed: 'Loading user data failed!',
      logoutFailed: 'Logout failed!',
    },
  },

  AccountsConfigurationPage: {
    heading: 'Accounts Administration',
    description: 'All system accounts can be configured on this page.',
    button: {
      addAccount: 'Add Account',
    },
    card: {
      heading: 'Accounts',
      button: {
        tooltip: {
          delete: 'Delete account',
          edit: 'Edit account',
        },
      },
    },
    dialog: {
      create: {
        heading: 'Create Account',
      },
      edit: {
        heading: 'Edit Account',
      },
      delete: {
        heading: 'Delete Account?',
        description:
          'Are you sure you want to delete this account? This action cannot be undone!',
        button: 'Delete Account',
      },
      user: "Has 'User' rights?",
      admin: "Has 'Administrator' rights?",
    },
  },

  AccountSettingsPage: {
    heading: 'Account Settings',
    description: 'Here you can configure your account settings.',
    card: {
      heading: 'Account',
    },
    apiToken: 'API Token',
  },

  AppearanceConfigurationPage: {
    heading: 'Appearance Configuration',
  },

  BatchProcessingPage: {
    heading: 'Batch Processing',
    startButton: 'Start batch processing',
    parameters: {
      task: 'Task',
      taskDescription:
        'Please select the task to perform on all selected items.',
      searchTerm: 'Item Selection',
      searchTermDescription:
        'Specify a search term which will select all items that should be processed.',
      targetId: 'Target',
      targetIdDescription: 'Please select the target of the task to perform.',
    },
    dialog: {
      process: {
        heading: 'Batch Processing',
        description:
          'Are you sure you want to start batch processing the selected items?',
        approve: 'Start batch processing',
      },
    },
    messages: {
      process: {
        success: 'Batch processing finished',
        failed: 'Batch processing failed!',
      },
    },
  },

  EditablePage: {
    dialog: {
      resetWip: {
        heading: 'Reset Page?',
        content:
          'Are you sure you want to reset the page to the latest published version? All changes will be lost!',
        approve: 'Reset',
      },
      publishWip: {
        heading: 'Publish Page?',
        content:
          'Do you really want to publish the current page configuration?',
        approve: 'Publish',
      },
    },
    resetWip: {
      success: 'Page reset',
      failed: 'Page reset failed!',
    },
    publishWip: {
      success: 'Page published',
      failed: 'Publishing page failed!',
    },
  },

  ErrorNotFoundPage: {
    heading: '404',
    description: 'Page not found...',
  },

  ExchangeConfigurationPage: {
    heading: 'Collection Exchange',
    configuration: {
      heading: 'Item Synchronization',
      description:
        'Here you can configure exchange parameters for syncing items with remote Artivact instances.',
    },
    tabs: {
      configuration: 'Configuration',
      export: 'Export',
      import: 'Import',
    },
    export: {
      heading: 'Export Configurations',
      description:
        'This page can be used to create and manage collection exports. Those can be used to transfer menus, their pages and items displayed on the pages to other Artivact instances. Additionally, the collection exports can be served by Artivact instances to be used e.g. in the Artivact XR app.',
    },
    import: {
      heading: 'Collection Import',
      description:
        'Here you can import previously created collection exports. Those can either be imported with all menus, pages and items or as packaged export for distribution only.',
      completeImport: 'Complete Import',
      forDistributionImport: 'Import for Distribution',
      exportFile: 'Collection Export',
    },
    dialog: {
      delete: {
        heading: 'Delete Export Configuration?',
        description:
          'Do you want to delete this collection export configuration and all associated files? This action cannot be undone!',
        approve: 'Delete Configuration',
      },
      build: {
        heading: 'Create Export File?',
        description:
          'Do you want to create the export file for this configuration now? This may take some time, depending on the collection size.',
        approve: 'Create Export File',
      },
    },
    messages: {
      buildExportFileFailed: 'Export file building failed!',
      importSuccess: 'Import successful',
    },
  },

  IndexPage: {
    messages: {
      noIndexPage: 'Could not load index page!',
    },
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
        description:
          'Are you sure you want to delete this item and all its files? This action cannot be undone!',
        button: 'Delete Item',
      },
    },
    messages: {
      sync: {
        success: 'Item synchronized',
        failed: 'Synchronization failed',
      },
    },
  },

  ItemEditPage: {
    button: {
      tooltip: {
        close: 'Leave edit mode and return to item page',
        save: 'Save item',
        addTag: 'Add tag to item',
        removeTag: 'Remove tag from item',
        deleteModel: 'Deletes the model from the item',
      },
    },
    tab: {
      base: 'Base Data',
      media: 'Media',
      properties: 'Properties',
      creation: 'Scan',
    },
    label: {
      tags: 'Tags:',
      noProperties:
        'There are currently no property definitions for items available. Go to the configuration page and add properties.',
    },
    dialog: {
      addTag: {
        heading: 'Add Tag',
      },
    },
    editor: {
      title: 'Title',
      description: 'Description',
    },
  },

  LoginPage: {
    login: 'Login',
    messages: {
      loginSuccessful: 'Login successful',
      loginFailed: 'Login failed',
      loadingUserdataFailed: 'Loading user data failed',
      loadingMenusFailed: 'Loading menus failed',
    },
  },

  PeripheralsConfigurationPage: {
    heading: 'Peripherals Configuration',
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
      description:
        'Here the properties of items can be configured. They are organized in categories, which can be ordered by drag & drop. The categories, with their respective properties, are shown at the bottom of the item details page.',
      noPropertiesDefined: 'There are currently no properties defined.',
    },
    export: {
      heading: 'Properties Export',
      description:
        'You can export the current properties configuration with the button below. A JSON file containing the current configuration will be created.',
      button: 'Export Properties',
    },
    import: {
      heading: 'Properties Import',
      description:
        'You can upload a previously created properties export here and OVERWRITE the current properties with it.',
      button: 'Import Properties',
    },
    messages: {
      uploadSuccess: 'Properties configuration uploaded',
    },
  },

  TagsConfigurationPage: {
    heading: 'Tags',
    configuration: {
      heading: 'Configuration',
      description:
        'Tags can be used to categorize items beyond their properties. They should be considered meta-data and not be used to replace properties.',
      noTagsDefined: 'There are currently no tags defined.',
    },
    export: {
      heading: 'Export',
      description:
        'You can export the current tags configuration with the button below. A JSON file containing the current configuration will be created.',
      button: 'Export Tags',
    },
    import: {
      heading: 'Import',
      description:
        'You can upload a previously created tags export here and OVERWRITE the current tags with it.',
      button: 'Import Tags',
    },
    messages: {
      uploadSuccess: 'Tags configuration uploaded',
    },
  },

  ArtivactAppearanceConfigurationEditor: {
    description: 'Configures the appearance of the Artivact application.',
    list: {
      title: {
        heading: 'Application Title',
        description: 'The title is displayed in the browser tab on every page.',
        label: 'Application Title',
      },
      indexPage: {
        heading: 'Index Page',
        description:
          'The index page is loaded as default by the application if no specific page is opened by the user.',
        label: 'Index Page',
      },
      locales: {
        heading: 'Supported Locales',
        description:
          "Comma-separated list of locales supported by this installation. E.g. 'de,nl,ja'. Those locales can be maintained in addition to the default locale by choosing it from the settings menu and editing translatable strings e.g. on pages.",
        label: 'Locales',
      },
      applicationLocale: {
        heading: 'Application Locale',
        description:
          "Here you can select the locale used by the application, i.e. the language of all texts of the application, that are not edited by the user. By default, the system locale is used if it is supported by the application, or english if it isn't supported.",
        label: 'Locale',
        de: 'German',
        en: 'English',
        system: 'System language',
      },
      colors: {
        heading: 'Color Theme',
        description:
          'The color theme can be customized with the following settings.',
      },
      favicon: {
        heading: 'Favicon',
        descriptionPrefix:
          'A custom Favicon can be uploaded here. The favicon file must be in ICO (.ico) format!',
        description: 'The uploaded file is directly installed as favicon!',
        descriptionSuffix:
          'Due to browser caching the newly uploaded favicon might not directly be visible in your browser tab.',
        saved: 'Favicon saved',
      },
      license: {
        heading: 'Media Files License',
        description:
          'Configures the license string displayed on the artivacts details-page. The license is composed of a prefix, the actual license and a suffix. If an URL is configured, the license will be rendered as link to the provided URL.',
        descriptionSuffix:
          'The final license text might look like: "Media files are provided under \'CC BY-SA\' license."',
        card: {
          heading: 'License',
          prefixDescription:
            "The first part of the license text, e.g. 'Media files are provided under'.",
          prefixLabel: 'Prefix',
          licenseDescription:
            "The name of the license, e.g. 'MIT' or 'CC BY-SA'.",
          licenseLabel: 'License',
          suffixDescription:
            "The last part of the license text, e.g. 'license'.",
          suffixLabel: 'Suffix',
          urlDescription:
            'If provided, this URL will be used to make the license text into a link.',
          urlLabel: 'License-URL',
        },
      },
    },
  },

  ArtivactButtonEditor: {
    label: {
      targetUrl: 'Target URL',
      iconLeft: 'Left icon',
      label: 'Button label',
      iconRight: 'Right icon',
      size: 'Button size',
      buttonColor: 'Button color',
      textColor: 'Text color',
    },
  },

  ArtivactCollectionExportEditor: {
    dialog: {
      create: {
        heading: 'Create Export Configuration',
        approve: 'Create Configuration',
      },
      edit: {
        heading: 'Edit Export Configuration',
        approve: 'Save Configuration',
      },
    },
    label: {
      sourceId: 'Source of collection export',
      title: 'Title',
      description: 'Short Description',
      content: 'Content',
      optimizeSize: 'Optimize export size',
      applyRestrictions: 'Exclude restricted elements',
      exportFileMissing:
        'No export file exists for this configuration! You can create one by clicking on the respective button above.',
      lastModified: 'Last modification of export file: ',
      coverPicture: 'Cover Picture',
      noCoverPicture: 'No cover picture available.',
    },
    help: {
      title: "The collection export's title.",
      description:
        "A short (one to two sentences) description of the export's content.",
      content: "A longer description of the export's content.",
    },
    button: {
      create: 'Create Export Configuration',
    },
    tooltip: {
      delete: 'Delete this export configuration and associated files',
      edit: 'Edit this export configuration',
      build:
        'Re-create the export file for this collection export configuration',
      buildNew:
        'Create a new export file for this collection export configuration',
      download: 'Download the export file',
      deleteCoverPicture: 'Delete cover picture from collection export',
      restricted: 'Access to this collection export is restricted',
      distributionOnly: 'This collection export is for distribution only',
    },
  },

  ArtivactContentExportConfigurationEditor: {
    tooltip: {
      deleteContentExport: 'Delete Export',
    },
    label: {
      lastModified: 'Last modified: ',
    },
  },

  ArtivactExchangeConfigurationEditor: {
    synchronization: {
      heading: 'Synchronization Configuration',
      server: {
        description:
          'URL of the remote Artivact server you want to use to synchronize items.',
        label: 'Remote Artivact Server',
      },
      token: {
        description:
          'API token to use for synchronization. The token configured here must also be configured for a user account on the remote server. This user will be used on the remote side for authentication and authorization.',
        label: 'API Token',
      },
    },
  },

  ArtivactItemModelEditor: {
    tooltip: {
      open: 'Open the directory containing the 3D model.',
      upload: 'Upload an existing GLTF/GLB 3D model to this item.',
      delete: 'Remove model from item',
      move: 'Change sort order per Drag&Drop.',
    },
    dialog: {
      upload: {
        heading: 'Upload 3D model',
        label: 'Add model',
      },
      delete: {
        heading: 'Remove 3D model?',
        description:
          'Are you sure you want to delete this model? This action cannot be undone!',
        approve: 'Delete model',
      },
      messages: {
        openFailed: 'Could not open model directory!',
      },
    },
  },

  ArtivactMenuBar: {
    tooltip: {
      edit: 'Right click to edit menu',
    },
    label: {
      menu: 'Menu',
      targetPageAlias: 'URL Alias',
      hidden: 'Hidden Menu',
      external: 'External Page URL',
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
        excludeItems: 'Exclude items from export',
      },
    },
    dialog: {
      add: 'Add Menu',
      edit: 'Edit Menu',
      addEntry: 'Add Menu Entry',
      editEntry: 'Edit Menu Entry',
      description: "Enter the menu's name.",
      descriptionEntry: "Enter the menu entry's name.",
      descriptionTargetPageAlias:
        "An optional alias which can be entered in the URL instead of the page's ID.",
      descriptionHidden:
        'The menu is hidden to unauthenticated users, but the page is still accessible via URL!',
      descriptionExternal:
        'An URL pointing to an external web page. The URL is opened in a new browser window.',
      delete: 'Delete Menu?',
      deleteEntry: 'Delete Menu Entry?',
      deleteDescription:
        'Are you sure you want to delete this menu including its page and menu entries? This action cannot be undone!',
      deleteApprove: 'Delete Menu',
      deleteApproveEntry: 'Delete Menu Entry',
      exportConfiguration: 'Export Configuration',
      exportApprove: 'Export Content',
      import: 'Import Menu',
      importDescription:
        'Please select a previously exported menu and upload the export file here.',
      importFileUpload: 'Export File Upload',
    },
    messages: {
      movingFailed: 'Moving failed',
      exportFinished: 'Export finished',
    },
  },

  ArtivactOperationInProgressDialog: {
    heading: 'Operation in Progress',
    failedHeading: 'Operation Failed',
    details: 'Details',
    successMessage: 'The operation finished successfully.',
    errorMessage: 'The operation failed.',
  },

  ArtivactPage: {
    tooltip: {
      edit: 'Edit page',
      cancel: 'Leave Edit Mode',
      add: 'Add Widget',
      resetWip: 'Reset Page',
      publishWip: 'Publish Page',
      editMetadata: 'Edit Metadata',
    },
    label: {
      noIndexPage:
        'No index page has been defined yet. Create a menu, add a page to it and edit it to be the index page.',
      addWidget: 'Add Widget',
      deleteWidget: 'Delete Widget',
      pageTitle: 'Page Title',
      textTitle: 'Text Title',
      textContent: 'Text Content',
      text: 'Text',
      infoBoxTitle: 'Info-Box Title',
      infoBoxContent: 'Info-Box Content',
      avatarSubtext: 'Avatar Subtext',
    },
    dialog: {
      addWidget: {
        heading: 'Widget Selection',
        description:
          'WARNING: Adding a new widget will save the current page configuration!',
        type: 'Widget Type',
      },
      deleteWidget: {
        heading: 'Delete Widget',
        description: 'Are you sure you want to delete this widget?',
      },
      editMetadata: {
        heading: 'Edit Metadata',
        description:
          'You can edit the metadata of this page here. The values will be used in the HTML HEAD element.',
        label: {
          title: 'Title',
          description: 'Description',
          author: 'Author',
          keywords: 'Keywords',
        },
        desc: {
          title: 'The page title displayed in the browser tab.',
          description: 'A short description of the page.',
          author: 'The author of the page.',
          keywords: 'Comma-separated list of Keywords for the page.',
        },
      },
    },
  },

  ArtivactPeripheralsConfigurationEditor: {
    description:
      'Configures the peripherals for 3D model creation of the Artivact application. Fallback-Options can be used if the peripheral or external software can not be used at all.',
    turntable: {
      heading: 'Turntable Configuration',
      description:
        'Automatic rotation of captured items via turntables can be configured here. Currently only the open source Artivact turntable is supported. If you use a turntable manually you can configure the fallback option and set a delay to give you time to rotate the turntable by hand.',
      label: 'Turntable to use',
      delay: 'Turntable delay in milliseconds',
    },
    camera: {
      heading: 'Camera Configuration',
      description:
        'Images are captured with third party applications, which can be configured here. On Windows, DigiCamControl is supported. On Linux, gphoto2 must be used.',
      label: 'Photo-Capture Software to use',
      digiCamControlExe: 'DigiCamControl Executable',
      digiCamControlUrl: 'DigiCamControl Webserver URL',
      gphotoExe: 'gphoto2 Executable',
    },
    background: {
      heading: 'Background Removal',
      description:
        'Automatic background removal of captured images is implemented using open-source neural networks for salient object detection. You can find out more in the documentation. The default configuration is:',
      defaultConfiguration: 'silueta.onnx#input.1#320#320#5',
      label: 'Background-Removal Software to use',
      default: 'Configuration String',
    },
    creator: {
      heading: '3D Model-Creator',
      description:
        'For 3D model creation currently "Metashape", "Meshroom" and "RealityScan" are supported.',
      label: 'Photogrammetry Software to use',
      meshroom: 'Meshroom Executable',
      metashape: 'Metashape Executable',
      RealityScan:
        'RealityScan Executable. Append "#headless#12345" to run in headless mode and limit faces to 12345.',
    },
    editor: {
      heading: '3D Model-Editor',
      description:
        'For editing created 3D models, Blender3D can be configured here.',
      label: '3D Model Editor to use',
      blender: 'Blender3D Executable',
    },
  },

  ArtivactPropertiesConfigurationEditor: {
    tooltip: {
      delete: 'Delete Category',
      deleteProperty: 'Delete Property',
      switchCategory: 'Move property to a different category',
      up: 'Move property up',
      down: 'Move property down',
    },
    button: {
      addProperty: 'Add Property',
      addCategory: 'Add Category',
    },
    newCategory: 'New Category',
    newProperty: 'New Property',
  },

  ArtivactPropertyValueRangeEditor: {
    label: 'Range of values:',
    tooltip: {
      edit: 'Edit value',
      delete: 'Delete value',
      add: 'Add value',
    },
    dialog: {
      heading: 'Configure Value',
      label: 'Value',
    },
    newValue: 'New Value',
  },

  ArtivactRestrictedTranslatableItemEditor: {
    default: 'Default: ',
    tooltip: {
      more: 'Show details',
      less: 'Hide details',
    },
  },

  ArtivactRestrictionsEditor: {
    restrictions: 'Restrictions:',
  },

  ArtivactSettingsBar: {
    tooltip: {
      locales: 'Locale Selection',
      systemSettings: 'System Settings',
      account: 'Account Settings',
      documentation: 'Open the documentation',
    },
    default: 'Default',
    itemSettings: 'Item Settings',
    createItem: 'Create Item',
    scanItem: 'Scan Item',
    importItem: 'Import Item',
    license: 'License',
    appearance: 'Appearance',
    peripherals: 'Peripherals',
    exchange: 'Exchange',
    locale: 'Locale',
    exhib: 'Exhib.',
    system: 'System',
    documentation: 'Docs',
    batch: 'Batch Processing',
    dialog: {
      import: 'Import Item',
      importDescription:
        'Import a previously exported item. Please select the export file and upload it here.',
      importFileUpload: 'Export File Upload',
    },
  },

  ArtivactTagsConfigurationEditor: {
    tooltip: {
      delete: 'Delete Tag',
    },
    url: 'URL',
    addTag: 'Add Tag',
    newTag: 'New Tag',
  },

  ItemImageEditor: {
    tooltip: {
      open: 'Open the directory containing the images.',
      upload: 'Upload an existing JPG/PNG image to this item.',
      delete: 'Remove image from item',
      move: 'Change sort order per Drag&Drop.',
    },
    dialog: {
      upload: {
        heading: 'Upload image',
        label: 'Add image',
      },
      delete: {
        heading: 'Delete Image?',
        description:
          'Are you sure you want to delete this image? This action cannot be undone!',
        approve: 'Delete image',
      },
      messages: {
        openFailed: 'Could not open model directory!',
      },
    },
  },

  ItemImageSetEditor: {
    tooltip: {
      capture: 'Capture photos into new image set',
      open: 'Open directory containing images',
      upload: 'Upload existing images into new image set',
      details: 'Show image set details',
      backgrounds: 'Remove image backgrounds',
      delete: 'Delete image set',
      directCapture: 'Capture single photo into item media',
    },
    label: {
      numPhotos: 'Number of photos',
      turntable: 'Use Turntable?',
      delay: 'Turntable Delay',
      backgrounds: 'Remove image backgrounds?',
      add: 'Add',
    },
    captureParameters: 'Photo-Capture Parameters',
    startCapturing: 'Start Capturing',
    transferPhotoToMedia: 'Add Photo to Media?',
    dialog: {
      upload: {
        heading: 'Upload Files to new Image-Set',
        label: 'Add Images',
      },
      details: {
        heading: 'Image-Set Details',
        transfer: 'Transfer image to item media',
        deleteImage: 'Delete image',
      },
      delete: {
        heading: 'Delete Image-Set?',
        description:
          'Are you sure you want to delete this Image-Set and all its files? This action cannot be undone!',
        approve: 'Delete Image-Set',
      },
      captureSinglePhotoInProgress: {
        heading: 'Capturing Photo',
        description: 'Capturing single photo to import as item media.',
      },
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
      operationFailed: 'Operation failed!',
    },
  },

  ItemMediaCarousel: {
    tooltip: {
      image: 'Show images',
      model: 'Show 3D models',
    },
  },

  ItemModelSetEditor: {
    tooltip: {
      create: 'Create model from image sets',
      open: 'Open directory containing model directories',
      details: 'Show model files',
      openModel: 'Open directory containing model files',
      edit: 'Edit 3D model in editor',
      delete: 'Delete 3D model',
    },
    dialog: {
      details: {
        heading: 'Model-Set Details',
        transfer: 'Transfer 3D model to item media',
      },
      delete: {
        heading: 'Delete Model-Set?',
        description:
          'Are you sure you want to delete this Model-Set and all its files? This action cannot be undone!',
        approve: 'Delete Model-Set',
      },
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
      operationFailed: 'Operation failed',
    },
  },

  ItemModelViewer: {
    downloadModelButtonLabel: 'Download Model',
  },

  ArtivactItemSearchInput: {
    error: 'Please enter a search query.',
    label: {
      maxResults: 'Max Results',
      term: 'Search Term',
      fulltext:
        'Syntax for searching property values in fulltext search: "PROPERTY_ID=[SEARCH_TERM]"',
      property:
        'Syntax for searching property values only: PROPERTY_ID:"[SEARCH_TERM]"',
      addTag: 'Add Tag',
      addProperty: 'Add Property',
    },
  },

  Widget: {
    label: {
      navigationTitle: 'Navigation',
    },
  },

  AvatarWidget: {
    label: {
      image: 'Avatar Image',
      subtext: 'Avatar Subtext',
    },
  },

  ButtonsWidget: {
    label: {
      columns: 'Grid columns',
      addButton: 'Add button',
    },
  },

  ImageGalleryWidget: {
    label: {
      heading: 'Heading',
      content: 'Content',
      images: 'Images',
      fullscreenAllowed:
        'Configures whether the images can be opened in fullscreen detail view.',
      iconMode: 'Display images as smaller icons.',
      hideBorder: 'Hides the border around the gallery.',
      textPositionTop: 'Show images below text',
      textPositionLeft: 'Show images right of text',
      textPositionRight: 'Show images left of text',
    },
  },

  InfoBoxWidget: {
    label: {
      heading: 'Heading',
      content: 'Content',
      outlined: 'Outlined',
      typeInfo: 'Info-Box',
      typeWarn: 'Warning-Box',
      typeAlert: 'Alert-Box',
    },
  },

  PageTitleWidget: {
    label: {
      bgImage: 'Background Image',
      title: 'Title',
    },
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
    },
  },

  TextWidget: {
    label: {
      heading: 'Heading',
      content: 'Content',
    },
  },

  WidgetTemplate: {
    tooltip: {
      edit: 'Right click to edit widget',
    },
    label: {
      edit: 'Edit Widget',
      close: 'Close Widget Editor',
      moveUp: 'Move Widget Up',
      moveDown: 'Move Widget Down',
      delete: 'Delete Widget',
      addAbove: 'Add Widget Above',
      addBelow: 'Add Widget Below',
    },
  },

  Progress: {
    manipulateImage: {
      backgroundRemovalStart: 'Start removing backgrounds from images.',
      backgroundRemovalInProgress: 'Removing backgrounds from images...',
      failed: 'Removing backgrounds from images failed!',
    },
    captureImages: {
      start: 'Start capturing images.',
      inProgress: 'Capturing images...',
      imageSetInProgress: 'Adding images to image-set...',
      failed: 'Capturing images failed!',
    },
    createImageSet: {
      start: 'Importing images to new image-set.',
      failed: 'Importing images failed!',
    },
    createModel: {
      createModelStart: 'Creating model in external editor.',
      copyImages: 'Copying images...',
      failed: 'Error during model creation!',
    },
    editModel: {
      start: 'Editing model in external editor.',
      failed: 'Error during model editing!',
    },
    search: {
      createIndex: '(Re-)Creating search index...',
      failed: '(Re-)Creation of search index failed!',
    },
    itemUpload: {
      uploading: 'Uploading file to remote Artivact server.',
      failed: 'Could not upload item file to remote server!',
    },
    batch: {
      process: 'Batch processing...',
      failed: 'Batch processing failed!',
    },
    collectionImport: {
      import: 'Importing collection...',
      failed: 'Import failed!',
    },
    collectionExport: {
      export: 'Building collection export file...',
      failed: 'Operation failed!',
    },
  },

  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'User',

  PAGE_TITLE: 'Page Title',
  PAGE_TITLE_DESCRIPTION: 'Page title with hero image and heading.',
  TEXT: 'Text',
  TEXT_DESCRIPTION:
    'Text block with optional heading. Supports Markdown for text formatting.',
  ITEM_SEARCH: 'Item Search',
  ITEM_SEARCH_DESCRIPTION:
    'Either presents the results of a predefined item search, or lets the user input search parameters and displays the results.',
  INFO_BOX: 'Info Box',
  INFO_BOX_DESCRIPTION:
    'A text box with optional heading that can be colored according to their three states: INFO, WARN and ALERT.',
  AVATAR: 'Avatar',
  AVATAR_DESCRIPTION:
    'A portrait image with optional subtext and description to the right.',
  IMAGE_GALLERY: 'Image Gallery',
  IMAGE_GALLERY_DESCRIPTION:
    'An image gallery with optional heading and content text.',
  BUTTONS: 'Buttons',
  BUTTONS_DESCRIPTION: 'A grid of one or more configurable buttons.',

  DEFAULT_TURNTABLE_PERIPHERAL: 'Default',
  DEFAULT_IMAGE_MANIPULATION_PERIPHERAL: 'Default',
  DEFAULT_CAMERA_PERIPHERAL: 'Default',
  DIGI_CAM_CONTROL_CAMERA_PERIPHERAL: 'DigiCamControl',
  GPHOTO_TWO_CAMERA_PERIPHERAL: 'gphoto2',
  FALLBACK_MODEL_CREATOR_PERIPHERAL: 'Fallback',
  MESHROOM_MODEL_CREATOR_PERIPHERAL: 'Meshroom',
  METASHAPE_MODEL_CREATOR_PERIPHERAL: 'Metashape',
  REALITY_SCAN_MODEL_CREATOR_PERIPHERAL: 'RealityScan',
  FALLBACK_MODEL_EDITOR_PERIPHERAL: 'Fallback',
  BLENDER_MODEL_EDITOR_PERIPHERAL: 'Blender3D',

  DELETE_ITEM: 'Delete item',
  ADD_TAG_TO_ITEM: 'Add tag',
  REMOVE_TAG_FROM_ITEM: 'Remove tag',
  UPLOAD_MODIFIED_ITEM: 'Upload modified item',
  UPDATE_SEARCH_INDEX: 'Update search index',
};
