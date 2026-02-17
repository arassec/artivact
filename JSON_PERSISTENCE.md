# JSON Persistence Feature

## Overview

This feature automatically persists entities (Items, Menus, and PageContent) as JSON files in the project root directory whenever they are created, updated, or deleted. This provides a file-based backup and makes it easy to version control or export entity data.

## Configuration

The feature is disabled by default and can be enabled via configuration:

```properties
# Enable/disable JSON persistence feature (default: false)
artivact.json.persistence.enabled=true
```

## Directory Structure

JSON files are organized in a hierarchical directory structure based on entity IDs:

```
projectRoot/
├── items/
│   └── abc/
│       └── 123/
│           └── abc123def456.json
├── menus/
│   └── men/
│       └── u12/
│           └── menu123456.json
└── pages/
    └── pag/
        └── e12/
            └── page123456.json
```

The directory structure uses:
- First subdirectory: First 3 characters of the entity ID
- Second subdirectory: Characters 4-6 of the entity ID
- File name: Full entity ID with `.json` extension

## Implementation Details

### Annotations

Two custom annotations are provided:

1. **`@PersistAsJson("entityType")`** - Applied to methods that save entities
   - Executed AFTER the method returns successfully
   - Serializes the entity to JSON and writes to the appropriate directory
   - Handles single entities and collections

2. **`@DeleteEntityJson("entityType")`** - Applied to methods that delete entities
   - Executed BEFORE the method executes
   - Removes the JSON file and cleans up empty parent directories

### Annotated Methods

#### Items
- `ManageItemService.save(Item)` - Persists item as JSON
- `ManageItemService.delete(String)` - Deletes item JSON

#### Menus
- `ManageMenuService.saveMenu(Menu)` - Persists menu as JSON
- `ManageMenuService.saveMenus(List<Menu>)` - Persists all menus as JSON
- `ManageMenuService.deleteMenu(String)` - Deletes menu JSON

#### Pages
- `ManagePageService.savePageContent(...)` - Persists page content as JSON
- `ManagePageService.deletePage(String)` - Deletes page JSON

### Error Handling

The aspect is designed to never interrupt the main application flow:
- All exceptions are caught and logged
- Failures in JSON persistence do not affect database operations
- If the feature is disabled, no JSON operations are performed

### ID Requirements

- Entity IDs must be at least 6 characters long to be persisted
- Shorter IDs are skipped with a debug log message
- This prevents errors with the subdirectory structure

## Testing

Two test classes are provided:

1. **`PersistAsJsonAspectTest`** - Unit tests using mocks
2. **`PersistAsJsonAspectManualTest`** - Integration tests with real file system operations

## Architecture Compliance

This implementation follows the Hexagonal Architecture principles:
- Aspect is in the application layer
- Uses existing ports (UseProjectDirsUseCase, FileRepository)
- Minimal invasiveness - only method annotations added
- No changes to domain logic or existing business logic
