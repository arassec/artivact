# Exchange Configuration <Badge type="warning" text="desktop"/>

## Synchronization

Two configuration options must be set in order to upload items from a local installation to a remote instance of
Artivact:

- Remote Artivact Server
    - HTTP(S) URL to the remote Artivact instance, e.g. ``https://www.example.com/``
- API Token
    - A security token (String) configured in the account settings on the remote instance. This user account will be
      used for upload.

## Content Exports

Content exports of parts of or event your entire collection can be managed here.

Base for the content export is always a menu. The associated page and all the items shown on it will be included in the
export.

Additionally, the export configuration offers the following options:

| Option                      | Description                                                                                                                                                         |
|:----------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Restrictions                | Content exports can be restricted to administrators and/or users.                                                                                                   | 
| Title                       | A short title of the export which will be included in the export itself.                                                                                            | 
| Short Description           | A short summary of the content export's contents.                                                                                                                   | 
| Content                     | A longer, more detailed description of the export's contents.                                                                                                       | 
| Optimize export size        | Optimized exports will contain a single 3D model for each item if available, or as fallback a single picture of the item, instead of all of the item's media files. | 
| Exclude restricted elements | Excludes all restricted elements, widgets or items, from the export.                                                                                                | 

## Content Imports

Previously created content exports can be imported into Artivact. Two options are available for content import:

- Complete Import: The content is imported including all items, pages and menus.
- Import for Distribution: Only the content export file is imported for further distribution, without importing included
  items, pages and menus.