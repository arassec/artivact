export default {

  Common: {
    cancel: 'Abbrechen',
    save: 'Speichern',
    apply: 'Anwenden',
    username: 'Benutzername',
    password: 'Passwort',
    passwordRepeat: 'Passwort (wdh.)',
    email: 'E-Mail',
    messages: {
      creating: {
        success: '{item} erzeugt',
        failed: 'Erzeugung von \'{item}\' fehlgeschlagen'
      },
      loading: {
        success: '{item} geladen',
        failed: 'Laden von \'{item}\' fehlgeschlagen',
      },
      saving: {
        success: '{item} gespeichert',
        failed: 'Speichern von \'{item}\' fehlgeschlagen'
      },
      deleting: {
        success: '{item} gelöscht',
        failed: 'Löschen von \'{item}\' fehlgeschlagen'
      }
    },
    items: {
      accounts: 'Benutzerkonten',
      account: 'Benutzerkonto',
      locales: 'Sprachen',
      page: 'Seite',
      items: 'Objekte',
      item: 'Objekt',
      category: 'Kategorie',
      properties: 'Eigenschaften',
      property: 'Eigenschaft',
      tags: 'Tags',
      tag: 'Tag',
      exports: 'Exporte',
      export: 'Export',
      menus: 'Menüs',
      menu: 'Menü',
      applicationLocale: 'Anwendungssprache',
      configuration: {
        appearance: 'Darstellungskonfiguration',
        exchange: 'Austausch-Konfiguration',
        license: 'Lizenzkonfiguration',
        peripherals: 'Peripherie-Konfiguration',
        properties: 'Eigenschaften-Konfiguration',
        tags: 'Tags-Konfiguration'
      }
    }
  },

  MainLayout: {
    login: 'Login',
    logout: 'Logout',
    messages: {
      colorThemeFailed: 'Laden des Farbschemas fehlgeschlagen!',
      rolesFailed: 'Laden von Benutzerrollen fehlgeschlagen!',
      titleFailed: 'Laden des Anwendungstitels fehlgeschlagen!',
      userDataFailed: 'Laden der Benutzerdaten fehlgeschlagen!',
      licenseFailed: 'Laden von Lizenzinformationen fehlgeschlagen!',
      logoutFailed: 'Logout fehlgeschlagen!'
    }
  },

  AccountsConfigurationPage: {
    heading: 'Benutzerkontenverwaltung',
    description: 'Alle Benutzerkonten des Systems können auf dieser Seite konfiguriert werden.',
    button: {
      addAccount: 'Benutzerkonto hinzufügen'
    },
    card: {
      heading: 'Benutzerkonten',
      button: {
        tooltip: {
          delete: 'Benutzerkonto löschen',
          edit: 'Benutzerkonto bearbeiten'
        },
      }
    },
    dialog: {
      create: {
        heading: 'Benutzerkonto erzeugen'
      },
      edit: {
        heading: 'Benutzerkonto bearbeiten'
      },
      delete: {
        heading: 'Benutzerkonto löschen?',
        description: 'Sind Sie sicher, dass Sie das ausgewählte Benutzerkonto löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
        button: 'Benutzerkonto löschen'
      },
      user: 'Hat \'Nutzer\'-Berechtigungen?',
      admin: 'Hat \'Administrator\'-Berechtigungen?'
    }
  },

  AccountSettingsPage: {
    heading: 'Benutzerkonto Einstellungen',
    description: 'Hier können Sie ihr Benutzerkonto verwalten.',
    card: {
      heading: 'Benutzerkonto'
    },
    apiToken: 'API Token'
  },

  AppearanceConfigurationPage: {
    heading: 'Darstellungs-Konfiguration'
  },

  EditablePage: {
    dialog: {
      heading: 'Änderungen Verwerfen?',
      content: 'Die neue Seitenkonfiguration wurden noch nicht gespeichert. Die Änderungen gehen beim Verlassen der Seite verloren. Möchten Sie trotzdem fortfahren',
      approve: 'Fortfahren'
    }
  },

  ErrorNotFoundPage: {
    heading: '404',
    description: 'Seite nicht gefunden...'
  },

  ExchangeConfigurationPage: {
    heading: 'Austausch-Konfiguration',
    exchange: {
      heading: 'Synchronisation',
      description: 'Hier können Parameter für den Austausch von Objekten zwischen verschiedenen Artivact-Instanzen konfiguriert werden.'
    },
    syncAllUp: {
      button: 'Alle geänderten Objekte Hochladen'
    },
    contentExport: {
      heading: 'Exporte',
      description: 'Hier können Exporte verwaltet werden. Exporte können per Rechtsklick auf ein Menu mit der Auswahl \'Inhalte Exportieren\' erstellt werden.'
    },
    dialog: {
      delete: {
        heading: 'Export löschen?',
        description: 'Sind Sie sicher, dass Sie den gewählten Export löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
        approve: 'Export löschen'
      }
    },
    messages: {
      sync: {
        success: 'Items synchronized',
        failed: 'Synchronization failed'
      }
    }
  },

  ItemDetailsPage: {
    button: {
      tooltip: {
        delete: 'Objekt löschen',
        sync: 'Objekt mit zentralem Server synchronisieren',
        edit: 'Objekt bearbeiten'
      },
    },
    dialog: {
      delete: {
        heading: 'Objekt löschen?',
        description: 'Sind Sie sicher, dass Sie das gewählte Objekt löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
        button: 'Objekt löschen'
      }
    },
    messages: {
      sync: {
        success: 'Objekt synchronisiert',
        failed: 'Synchronisation fehlgeschlagen'
      }
    }
  },

  ItemEditPage: {
    button: {
      tooltip: {
        close: 'Bearbeiten-Modus verlassen und zur Objekt-Seite zurückkehren',
        download: 'Objekt-Inhalte als ZIP-Datei herunterladen',
        save: 'Objekt speichern',
        addTag: 'Tag zu Objekt hinzufügen',
        removeTag: 'Tag von Objekt entfernen',
        deleteModel: 'Löscht das 3D-Modell vom Objekt'
      }
    },
    tab: {
      base: 'Basisdaten',
      media: 'Medien',
      properties: 'Eigenschaften',
      creation: 'Gestaltung'
    },
    label: {
      tags: 'Tags:',
      images: 'Bilder',
      models: '3D-Modelle',
      noProperties: 'Momentan sind keine Eigenschaften für Objekte definiert. Nutzen Sie die Eigenschafts-Konfiguration um diese zu definieren.'
    },
    dialog: {
      addTag: {
        heading: 'Tag hinzufügen'
      },
      unsavedChanges: {
        heading: 'Änderungen Verwerfen?',
        content: 'Die neue Konfiguration wurden noch nicht gespeichert. Die Änderungen gehen beim Verlassen der Seite verloren. Möchten Sie trotzdem fortfahren',
        approve: 'Fortfahren'
      }
    },
    editor: {
      title: 'Titel',
      description: 'Beschreibung'
    }
  },

  ItemImportPage: {
    heading: 'Objekt-Import',
    description: {
      scan: 'Scant das Datenverzeichnis um neue Objekte hinzuzufügen oder vorhandene zu aktualisieren.',
      upload: 'Importiert ein zuvor exportiertes Objekt.'
    },
    button: {
      scan: 'Scan',
      upload: 'Objekt Importieren'
    },
    messages: {
      scanSuccessful: 'Scan erfolgreich',
      scanFailed: 'Scan fehlgeschlagen',
      itemUploaded: 'Objekt hochgeladen'
    }
  },

  LicenseConfigurationPage: {
    heading: 'Lizenz-Konfiguration',
  },

  LoginPage: {
    login: 'Login',
    messages: {
      loginSuccessful: 'Login erfolgreich',
      loginFailed: 'Login fehlgeschlagen',
      loadingUserdataFailed: 'Laden von Benutzerdaten fehlgeschlagen',
      loadingMenusFailed: 'Laden von Menueinträgen fehlgeschlagen'
    }
  },

  PeripheralsConfigurationPage: {
    heading: 'Peripherie-Konfiguration'
  },

  PropertiesConfigurationPage: {
    heading: 'Eigenschaften-Konfiguration',
    description: 'Hier können die Eigenschaften von Objekten definiert werden. Sie werden in Kategorien unterteilt, die per Drag&Drop sortiert werden können. Die Kategorien, mit ihren enthaltenen Eigenschaften, werden auf der Objektdetailseite angezeigt.',
    noPropertiesDefined: 'Aktuell sind keine Eigenschaften definiert.',
    importexport: {
      heading: 'Eigenschaften Import/Export',
      export: 'Sie können die aktuelle Eigenschaften-Konfiguration mit dem unten stehenden Button exportieren. Eine JSON-Datei mit der aktuellen Konfiguration wird erstellt und heruntergeladen.',
      import: 'Sie können eine vorher exportierte Eigenschaften-Konfiguration hochladen und damit die aktuelle Konfiguration ÜBERSCHREIBEN.',
      button: {
        export: 'Eigenschaften exportieren',
        import: 'Eigenschaften importieren'
      },
    },
    messages: {
      uploadSuccess: 'Eigenschaften-Konfiguration hochgeladen'
    }
  },

  SearchConfigurationPage: {
    heading: 'Suchmaschinenkonfiguration',
    label: 'Auf dieser Seite kann der Suchindex der intern verwendeten Suchmaschine (neu) erstellt werden, sollte dies nötig sein.',
    btnLabel: 'Suchindex (neu) erstellen',
    messages: {
      indexCreated: 'Suchindex erstellt/aktualisiert',
      indexCreationFailed: 'Erstellung/Aktualisierung des Suchindex fehlgeschlagen!'
    }
  },

  TagsConfigurationPage: {
    heading: 'Tags-Konfiguration',
    description: 'Tags können verwendet werden um Objekte zu kategorisieren. Sie sollten als Metadaten behandelt werden, und nicht verwendet werden um Eigenschaften zu ersetzen.',
    noTagsDefined: 'Aktuell sind keine Tags definiert.',
    importexport: {
      heading: 'Tags Import/Export',
      export: 'Sie können die aktuelle Tags-Konfiguration mit dem unten stehenden Button exportieren. Eine JSON-Datei mit der aktuellen Konfiguration wird erstellt und heruntergeladen.',
      import: 'Sie können eine zuvor exportierte Tags-Konfiguration hochladen um die aktuelle Konfiguration zu ÜBERSCHREIBEN.',
      button: {
        export: 'Tags exportieren',
        import: 'Tags importieren'
      },
    },
    messages: {
      uploadSuccess: 'Tags-Konfiguration hochgeladen'
    }
  },

  ArtivactAppearanceConfigurationEditor: {
    description: 'Konfiguriert das Erscheinungsbild der Anwendung.',
    list: {
      title: {
        heading: 'Anwendungstitel',
        description: 'Der Titel der Anwendung wird im Browser-Tab auf jeder Seite angezeigt.',
        label: 'Anwendungstitel'
      },
      locales: {
        heading: 'Unterstützte Sprachen',
        description: 'Kommaseparierte Liste mit Sprachcodes die von der Anwendung unterstützt werden. Beispiel: \'de,nl,ja\'. Die Sprachen können zusätzlich zur Standardsprache verwaltet werden, indem sie über die Sprachwahl in der Menuleiste ausgewählt, und anschließend die entsprechenden Texte editiert werden.',
        label: 'Sprachen'
      },
      applicationLocale: {
        heading: 'Anwendungssprache',
        description: 'Hier kann die Sprache der Anwendung konfiguriert werden, d.h. die Sprache der Texte innerhalb der Anwendung, die nicht vom Benutzer erzeugt werden. Ohne konfiguration wird die Systemsprache verwendet, falls diese unterstützt wird, oder Englisch bei nicht unterstützten Systemsprachen.',
        label: 'Sprache',
        de: 'Deutsch',
        en: 'Englisch',
        system: 'Systemsprache'
      },
      colors: {
        heading: 'Farbschema',
        description: 'Das Farbschema der Anwendung kann über die folgenden Einstellungen angepasst werden.',
      },
      favicon: {
        heading: 'Favicon',
        descriptionPrefix: 'Ein Favicon kann in den Größen 16x16 und 32x32 Pixel hochgeladen werden. Die Favicon-Datei muss im ICO (.ico) Format vorliegen!',
        description: 'Die hochgeladene Datei wird sofort als Favicon verwendet!',
        descriptionSuffix: 'Durch Browsercaches kann es sein, dass ein neu hochgeladenes Favicon erst nach einiger Zeit sichtbar wird.',
        saved: 'Favicon gespeichert'
      }
    }
  },

  ArtivactContentExportConfigurationEditor: {
    tooltip: {
      deleteContentExport: 'Export löschen'
    },
    label: {
      lastModified: 'Letzte Änderung: '
    }
  },

  ArtivactExchangeConfigurationEditor: {
    synchronization: {
      heading: 'Synchronisations-Konfiguration',
      server: {
        description: 'URL des Artivact-Servers, mit dem Objekte synchronisiert werden sollen.',
        label: 'Artivact-Server'
      },
      token: {
        description: 'API-Token zur Authentifizierung und Autorisierung bei der Synchronisation. Das hier konfigurierte Token muss ebenfalls im Artivact-Server einem Benutzerkonto über die Benutzerkontenverwaltung zugewiesen werden.',
        label: 'API-Token'
      }
    }
  },

  ArtivactModelEditor: {
    tooltip: {
      open: 'Verzeichnis mit 3D-Modell öffnen',
      upload: 'Bestehende GLTF/GLB-Datei zu Objekt hinzufügen.',
      delete: '3D-Modell löschen',
      move: 'Sortierung per Drag&Drop anpassen.'
    },
    dialog: {
      upload: {
        heading: '3D-Modell hochladen',
        label: '3D-Modell hinzufügen'
      },
      delete: {
        heading: '3D-Modell entfernen?',
        description: 'Sind Sie sicher, dass sie das ausgewählte 3D-Modell löschen möchten? Dies kann nicht rückgängig gemacht werden!!',
        approve: '3D-Modell löschen'
      },
      messages: {
        openFailed: 'Konnte Verzeichnis nicht öffnen!'
      }
    }
  },

  ArtivactLicenseConfigurationEditor: {
    description: 'Konfiguriert den Lizenz-Text auf der Objekt-Detailseite. Der Lizenztext setzt sich aus einem Prefix, der eigentlichen Lizenz und einem Suffix zusammen. Falls eine URL konfiguriert wird, wird die Lizenz als Link zu der konfigurierten URL gerendert.',
    descriptionSuffix: 'Der finale Lizenztext könnte folgendermaßen aussehen: "Mediendateien stehen unter der \'CC BY-SA\'-Lizenz."',
    card: {
      heading: 'Lizenz',
      prefixDescription: 'Der erste Teil des Lizenztextes, z.B. \'Mediendateien stehen unter der\'.',
      prefixLabel: 'Prefix',
      licenseDescription: 'Der Name der Lizenz, z.B. \'MIT\' oder \'CC BY-SA\'.',
      licenseLabel: 'Lizenz',
      suffixDescription: 'Der hintere Teil des Lizenztextes, z.B. \'-Lizenz\'.',
      suffixLabel: 'Suffix',
      urlDescription: 'Falls angegeben wird die Lizenz als Link zu dieser URL gerendert.',
      urlLabel: 'Lizenz-URL'
    }
  },

  ArtivactMenuBar: {
    tooltip: {
      edit: 'Rechtsklick zum Bearbeiten des Menüs'
    },
    label: {
      menu: 'Menü',
      edit: 'Menü bearbeiten',
      delete: 'Menü löschen',
      export: 'Inhalte exportieren',
      left: 'Nach links bewegen',
      right: 'Nach rechts bewegen',
      editEntry: 'Menüeintrag bearbeiten',
      deleteEntry: 'Menüeintrag löschen',
      up: 'Nach oben bewegen',
      down: 'Nach unten bewegen',
      add: 'Seite hinzufügen',
      addEntry: 'Menüeintrag hinzufügen',
      exportTitle: 'Export Titel',
      exportDescription: 'Export Beschreibung',
      exportParams: {
        optimizeSize: 'Exportgröße optimieren',
        zipResults: 'Exportverzeichnis packen (ZIP)',
        applyRestrictions: 'Eingeschränkte Elemente ausschließen',
      }
    },
    dialog: {
      add: 'Menü hinzufügen',
      edit: 'Menü bearbeiten',
      addEntry: 'Menüeintrag hinzufügen',
      editEntry: 'Menüeintrag bearbeiten',
      description: 'Geben Sie den Menünamen ein.',
      descriptionEntry: 'Geben Sie den Namen des Menüeintrags ein.',
      delete: 'Menü löschen?',
      deleteEntry: 'Menüeintrag löschen?',
      deleteDescription: 'Sind Sie sicher, dass Sie das ausgewählte Menü und alle seine Einträge und dazugehörigen Seiten löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
      deleteApprove: 'Menü löschen',
      deleteApproveEntry: 'Menüeintrag löschen',
      exportDescription: 'Optionaler Titel und Beschreibung für Exporte, die auf Basis dieses Menüs erstellt werden.',
      exportParams: 'Export Konfiguration',
      exportApprove: 'Inhalte exportieren'
    },
    messages: {
      movingFailed: 'Bewegen fehlgeschlagen!',
      exportFinished: 'Export abgeschlossen'
    }
  },

  ArtivactOperationInProgressDialog: {
    heading: 'Hintergrundprozess läuft',
    failedHeading: 'Hintergrundprozess fehlgeschlagen',
    details: 'Details'
  },

  ArtivactPage: {
    tooltip: {
      edit: 'Seite bearbeiten',
      cancel: 'Bearbeitungsmodus verlassen',
      add: 'Widget hinzufügen',
      indexPageYes: 'Dies IST die Hauptseite',
      indexPageNo: 'Dies ist NICHT die Hauptseite',
    },
    label: {
      noIndexPage: 'Es wurde noch keine Index-Seite definiert. Erstellen Sie ein Menu, fügen Sie eine Seite hinzu und konfigurieren Sie diese als Index-Seite.',
      addWidget: 'Widget hinzufügen',
      pageTitle: 'Seitentitel',
      textTitle: 'Texttitel',
      textContent: 'Textinhalt',
      text: 'Text',
      infoBoxTitle: 'Info-Box Titel',
      infoBoxContent: 'Info-Box Inhalt',
      avatarSubtext: 'Avatar Untertitel'
    },
    dialog: {
      heading: 'Widget-Auswahl',
      description: 'ACHTUNG: Das hinzufügen eines Widgets führt zum speichern der aktuellen Seitenkonfiguration!',
      type: 'Widget-Typ'
    }
  },

  ArtivactPeripheralsConfigurationEditor: {
    description: 'Konfiguriert Peripheriegeräte und -Software für die Erstellung von 3D-Modellen aus der Anwendung heraus. Fallback-Optionen können verwendet werden, sollten Geräte nicht vorhanden oder Software nicht installiert sein.',
    turntable: {
      heading: 'Drehteller-Konfiguration',
      description: 'Hier kann ein automatischer Drehteller für die Erzeugung von Bildern von Objekten aus verschiedenen Winkeln konfiguriert werden. Soll ein manueller Drehteller verwendet werden, kann die Fallback-Option mit einer Verzögerung konfiguriert werden, um den Drehteller zwischen der Erstellung von Fotos von Hand zu bedienen.',
      label: 'Verwendeter Drehteller',
      delay: 'Drehteller Verzögerung in Millisekunden'
    },
    camera: {
      heading: 'Kamera-Konfiguration',
      description: 'Bilder werden mithilfe von Drittanbietersoftware erstellt. Auf Windows-Systemen steht die Open-Source-Software \'DigiCamControl\' zur Verfügung, unter Linux-Systemen kann \'gphoto2\' verwendet werden.',
      label: 'Foto-Software',
      digiCamControlExe: 'DigiCamControl Exe-Datei',
      digiCamControlUrl: 'DigiCamControl Webserver URL',
      gphotoExe: 'gphoto2 Exe-Datei'
    },
    background: {
      heading: 'Automatisches Freistellen',
      description: 'Automatisches Freistellen der erzeugten Bilder wird mit dem Open-Source-Tool \'rembg\' von Daniel Gatis (https://github.com/danielgatis/rembg) realisiert. Dies kann z.B. über Docker bereitgestellt werden:',
      dockerCmd: 'docker run -d -p 5000:5000 --name=rembg --restart=always danielgatis/rembg s',
      label: 'Software zum automatischen Freistellen',
      rembg: 'rembg Webserver URL'
    },
    creator: {
      heading: '3D-Modell Erstellung',
      description: 'Zum erstellen von 3D-Modellen wird Photogrammetrie-Software verwendet. Momentan werden "Metashape", "Meshroom" und "RealityCapture" unterstützt.',
      label: 'Photogrammetrie-Software',
      meshroom: 'Meshroom Exe-Datei',
      metashape: 'Metashape Exe-Datei',
      RealityCapture: 'RealityCapture Exe-Datei. "#headless#12345" anhängen für Hintergrundverarbeitung und Face-Limit von 12345.'
    },
    editor: {
      heading: '3D-Modell Bearbeitung',
      description: 'Zur Bearbeitung von 3D-Modellen kann die Open-Source-Software Blender3D konfiguriert werden.',
      label: '3D-Modell Editor',
      blender: 'Blender3D Exe-Datei'
    }
  },

  ArtivactPropertiesConfigurationEditor: {
    tooltip: {
      delete: 'Kategorie löschen',
      deleteProperty: 'Eigenschaft löschen',
      switchCategory: 'Eigenschaft zu anderer Kategorie verschieben',
      up: 'Eigenschaft hoch bewegen',
      down: 'Eigenschaft herunter bewegen'
    },
    button: {
      addProperty: 'Eigenschaft hinzufügen',
      addCategory: 'Kategorie hinzufügen'
    },
    newCategory: 'Neue Kategorie',
    newProperty: 'Neue Eigenschaft'
  },

  ArtivactPropertyValueRangeEditor: {
    label: 'Wertebereich:',
    tooltip: {
      edit: 'Wert bearbeiten',
      delete: 'Wert löschen',
      add: 'Wert hinzufügen'
    },
    dialog: {
      heading: 'Wert konfigurieren',
      label: 'Wert'
    },
    newValue: 'Neuer Wert'
  },

  ArtivactRestrictedTranslatableItemEditor: {
    default: 'Standard: ',
    tooltip: {
      more: 'Details anzeigen',
      less: 'Details ausblenden'
    }
  },

  ArtivactRestrictionsEditor: {
    restrictions: 'Einschränkungen:'
  },

  ArtivactSettingsBar: {
    tooltip: {
      locales: 'Sprachauswahl',
      systemSettings: 'Systemeinstellungen',
      account: 'Benutzerkontokonfiguration',
      documentation: 'Dokumentation öffnen'
    },
    default: 'Standard',
    itemSettings: 'Objekt-Einstellungen',
    createItem: 'Objekt erzeugen',
    importItems: 'Objekte importieren',
    license: 'Lizenz',
    appearance: 'Darstellung',
    peripherals: 'Peripherie',
    exchange: 'Austausch',
    locale: 'Sprache',
    exhib: 'Ausstellung',
    system: 'System',
    search: 'Suchmaschine',
    documentation: 'Doku'
  },

  ArtivactTagsConfigurationEditor: {
    tooltip: {
      delete: 'Tag löschen',
    },
    url: 'URL',
    addTag: 'Tag hinzufügen',
    newTag: 'Neues Tag'
  },

  ItemImageEditor: {
    tooltip: {
      open: 'Verzeichnis mit Bildern öffnen',
      upload: 'Bestehende JPG/PNG-Datei zu Objekt hinzufügen.',
      delete: 'Bild löschen',
      move: 'Sortierung per Drag&Drop anpassen.'
    },
    dialog: {
      upload: {
        heading: 'Bild hochladen',
        label: 'Bild hinzufügen'
      },
      delete: {
        heading: 'Bild entfernen?',
        description: 'Sind Sie sicher, dass sie das ausgewählte Bild löschen möchten? Dies kann nicht rückgängig gemacht werden!!',
        approve: 'Bild löschen'
      },
      messages: {
        openFailed: 'Konnte Verzeichnis nicht öffnen!'
      }
    }
  },


  ItemImageSetEditor: {
    tooltip: {
      capture: 'Fotos per Kamera aufnehmen',
      open: 'Verzeichnis mit Bildern öffnen',
      upload: 'Bestehende Bilder in neues Bilder-Set hochladen',
      details: 'Bilder-Set Details anzeigen',
      backgrounds: 'Bilder freistellen',
      delete: 'Bilder-Set löschen',
    },
    label: {
      numPhotos: 'Anzahl Fotos',
      turntable: 'Drehteller benutzen?',
      delay: 'Drehteller Verzögerung',
      backgrounds: 'Bilder freistellen?'
    },
    captureParameters: 'Aufnahme-Parameter',
    startCapturing: 'Aufnahmen starten',
    dialog: {
      upload: {
        heading: 'Bilder in neues Bilder-Set hochladen',
        label: 'Bilder hochladen'
      },
      details: {
        heading: 'Bilder-Set Details',
        transfer: 'Bild in Objekt-Medien kopieren',
        deleteImage: 'Bild löschen'
      },
      delete: {
        heading: 'Bilder-Set löschen?',
        description: 'Sind Sie sicher, dass Sie das ausgewählte Bilder-Set löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
        approve: 'Bilder-Set löschen'
      }
    },
    messages: {
      capturingFailed: 'Aufnahmen fehlgeschlagen!',
      backgroundFailed: 'Feistellen fehlgeschlagen!',
      imageSetFailed: 'Bilder-Set Erstellung fehlgeschlagen!',
      openingFailed: 'Verzeichnis konnte nicht geöffnet werden!',
      transferred: 'Bild übertragen',
      transferFailed: 'Bild-Übertragung fehlgeschlagen',
      imageSetDeleted: 'Bilder-Set gelöscht',
      imageSetDeletionFailed: 'Löschung fehlgeschlagen',
      operationSuccess: 'Objekt gespeichert',
      operationFailed: 'Prozess fehlgeschlagen!'
    }
  },

  ItemMediaCarousel: {
    tooltip: {
      image: 'Bilder anzeigen',
      model: '3D-Modelle anzeigen',
      download: 'Mediendateien herunterladen'
    }
  },

  ItemModelSetEditor: {
    tooltip: {
      create: '3D-Modell aus Bilder-Sets erzeugen',
      open: 'Verzeichnis mit 3D-Modellen öffnen',
      details: '3D-Modell-Dateien anzeigen',
      openModel: 'Verzeichnis mit 3D-Modell-Dateien öffnen',
      edit: '3D-Modell in Editor bearbeiten',
      delete: '3D-Modell löschen'
    },
    dialog: {
      details: {
        heading: '3D-Modell Details',
        transfer: '3D-Modell in Objekt-Medien kopieren'
      },
      delete: {
        heading: '3D-Modell löschen?',
        description: 'Sind Sie sicher, dass Sie alle Dateien des ausgewählten 3D-Modells löschen möchten? Diese Aktion kann nicht rückgängig gemacht werden!',
        approve: '3D-Model löschen'
      }
    },
    messages: {
      loadingFailed: 'Laden des 3D-Modells fehlgeschlagen!',
      creationFailed: 'Erzeugung des 3D-Modells fehlgeschlagen!',
      openFailed: 'Verzeichnis konnte nicht geöffnet werden!',
      editingFailed: 'Bearbeitung des 3D-Modells fehlgeschlagen!',
      transferSuccess: '3D-Modell übertragen',
      transferFailed: '3D-Modell-Übertragung fehlgeschlagen!',
      deleteSuccess: '3D-Modell gelöscht',
      deleteFailed: 'Löschung des 3D-Modells fehlgeschlagen!',
      operationFailed: 'Prozess fehlgeschlagen!'
    }
  },

  ItemModelViewer: {
    downloadModelButtonLabel: '3D-Modell herunterladen',
  },

  ArtivactItemSearchInput: {
    error: 'Bitte geben Sie eine Suchabfrage ein.',
    label: {
      maxResults: 'Max Ergebnisse',
      term: 'Suchbegriff',
      fulltext: 'Syntax zur Suche von Eigenschaftswerten mit Volltextsuche: "PROPERTY_ID=[SEARCH_TERM]"',
      property: 'Syntax zur direkten Suche von Eigenschaftswerten: PROPERTY_ID:"[SEARCH_TERM]"',
      addTag: 'Tag hinzufügen',
      addProperty: 'Eigenschaft hinzufügen'
    }
  },

  AvatarWidget: {
    label: {
      image: 'Avatar Bild',
      subtext: 'Avatar Untertitel'
    }
  },

  ImageTextWidget: {
    label: {
      image: 'Bild',
      text: 'Text',
      fullscreenAllowed: 'Gibt an, ob das Bild als Vollbild-Detailansicht geöffnet werden kann.',
    }
  },

  InfoBoxWidget: {
    label: {
      heading: 'Überschrift',
      content: 'Inhalt',
      outlined: 'Art'
    }
  },

  PageTitleWidget: {
    label: {
      bgImage: 'Hintergrundbild',
      title: 'Titel'
    }
  },

  SearchWidget: {
    label: {
      noSearchResults: 'Keine Suchergebnisse verfügbar!'
    },
    messages: {
      noSearchResults: 'Keine Suchergebnisse verfügbar!',
      searchFailed: 'Suche fehlgeschlagen!'
    }
  },

  SpaceWidget: {
    label: {
      spaceUnits: 'Anzahl Abstände'
    }
  },

  TextWidget: {
    label: {
      heading: 'Überschrift',
      content: 'Inhalt'
    }
  },

  WidgetTemplate: {
    tooltip: {
      edit: 'Rechtsklick zum Bearbeiten des Widgets'
    },
    label: {
      edit: 'Widget bearbeiten',
      close: 'Widget Editor schließen',
      moveUp: 'Widget nach oben bewegen',
      moveDown: 'Widget nach unten bewegen',
      delete: 'Widget löschen',
      addAbove: 'Widget Oberhalb hinzufügen',
      addBelow: 'Widget Unterhalb hinzufügen',
    }
  },

  Progress: {
    MediaCreationService: {
      captureStart: 'Starte Fotoaufnahmen.',
      captureInProgress: 'Nehme Fotos auf...',
      captureFailed: 'Fotoaufnahme fehlgeschlagen!',
      rembgStart: 'Starte freistellen der Aufnahmen.',
      rembgInProgress: 'Stelle Aufnahmen frei...',
      rembgFailed: 'Freistellen von Aufnahmen fehlgeschlagen!',
      imageSetStart: 'Starte Import von Bildern in Bilder-Set.',
      imageSetInProgress: 'Füge Bilder zu Bilder-Set hinzu...',
      imageSetFailed: 'Fehler beim Hinzufügen von Bildern zu Bilder-Set!',
      createModelStart: 'Erstelle 3D-Modell in externem Editor.',
      createModelFailed: 'Fehler bei der Erstellung des 3D-Modells!',
      editModelStart: 'Bearbeite 3D-Modell in externem Editor.',
      editModelFailed: 'Fehler bei der Bearbeitung des 3D-Modells!',
      copyImages: 'Kopiere Bilder...'
    },
    SearchService: {
      createIndex: 'Erstelle Suchindex...',
      createIndexFailed: 'Fehler bei Erstellung/Aktualisierung des Suchindexes!',
    },
    ExportService: {
      exportContent: 'Erstelle Export...',
      exportContentFailed: 'Erstellung des Exports fehlgeschlagen!',
      packaging: 'Erstelle Exportdatei für Upload.',
      exportFileCreationFailed: 'Konnte Exportdatei für Upload nicht erstellen!',
      configMissing: 'Austausch-Konfiguration für Upload zu entferntem Artivact- Server fehlt!',
      uploading: 'Lade Exportdatei zu entferntem Artivact-Server hoch.',
      uploadFailed: 'Konnte Exportdatei nicht zu entferntem Artivact-Server hochladen!',
    }
  },

  ROLE_ADMIN: 'Admin',
  ROLE_USER: 'Benutzer',

  widgetType: 'Widget Typ',
  addWidgetWarn: 'WARNUNG: Durch hinzufügen eines neuen Widgets wird der aktuelle Zustand der Seite gespeichert!',
  PAGE_TITLE: 'Seitentitel',
  PAGE_TITLE_DESCRIPTION: 'Seitentitel mit Hero-Image (Hintergrundbild) und Überschrift.',
  TEXT: 'Text',
  TEXT_DESCRIPTION: 'Text mit optionaler Überschrift. Der Text unterstützt Markdown-Makros zur Formatierung.',
  IMAGE_TEXT: 'Bild und Text',
  IMAGE_TEXT_DESCRIPTION: 'Ein Bild und daneben stehender Text. Der Text unterstützt Markdown-Makros zur Formatierung.',
  ITEM_SEARCH: 'Item-Suche',
  ITEM_SEARCH_DESCRIPTION: 'Zeigt entweder das Ergebnis einer vordefinierten Item-Suche an, oder gibt den Nutzer die Möglichkeit selber nach Items zu suchen.',
  INFO_BOX: 'Info Box',
  INFO_BOX_DESCRIPTION: 'Eine text box mit optionaler Überschrift, die je nach Status eingefärbt wird: INFO, WARN and ALERT.',
  AVATAR: 'Avatar',
  AVATAR_DESCRIPTION: 'Ein Portraitbild mit optionalem Untertitel und Seitentext rechts stehend.',
  SPACE: 'Leer-Raum',
  SPACE_DESCRIPTION: 'Fügt leeren Raum von konfigurierbarer Größe auf der Seite ein.',

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

};
