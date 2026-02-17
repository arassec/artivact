# JSON Persistence Feature - Technical Overview

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Service Layer                            │
│  ┌───────────────┐  ┌────────────────┐  ┌────────────────┐ │
│  │ManageItemServ │  │ManageMenuServ  │  │ManagePageServ  │ │
│  │               │  │                │  │                │ │
│  │@PersistAsJson │  │@PersistAsJson  │  │@PersistAsJson  │ │
│  │save()         │  │saveMenu()      │  │savePageContent │ │
│  │               │  │saveMenus()     │  │                │ │
│  │@DeleteEntity  │  │@DeleteEntity   │  │@DeleteEntity   │ │
│  │delete()       │  │deleteMenu()    │  │deletePage()    │ │
│  └───────────────┘  └────────────────┘  └────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                         ↓
                    (AOP Pointcut)
                         ↓
┌─────────────────────────────────────────────────────────────┐
│              PersistAsJsonAspect (@Aspect)                  │
│                                                             │
│  @AfterReturning                    @Before                │
│  ├─ persistAsJson()                 ├─ deleteEntityJson()  │
│  │  └─ persistEntity()              │  └─ deleteEntityJsonFile() │
│  │     └─ jsonMapper.write()        │     └─ Files.delete()     │
│  │        └─ Files.writeString()    │        └─ cleanup empty dirs │
│                                                             │
│  Configuration: artivact.json.persistence.enabled=false    │
└─────────────────────────────────────────────────────────────┘
                         ↓
                   (Uses Ports)
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                    Infrastructure                           │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │ UseProjectDirsUseCase│  │   FileRepository      │        │
│  │ getProjectRoot()     │  │ createDirIfRequired() │        │
│  └──────────────────────┘  │ list()                │        │
│                            └──────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────────┐
│                     File System                             │
│                                                             │
│  projectRoot/                                               │
│  ├── items/                                                 │
│  │   └── abc/                                               │
│  │       └── 123/                                           │
│  │           └── abc123def456.json                          │
│  ├── menus/                                                 │
│  │   └── men/                                               │
│  │       └── u12/                                           │
│  │           └── menu123456.json                            │
│  └── pages/                                                 │
│      └── pag/                                               │
│          └── e12/                                           │
│              └── page123456.json                            │
└─────────────────────────────────────────────────────────────┘
```

## Flow Diagrams

### Save Flow

```
User calls service.save(entity)
         ↓
Service method executes (saves to DB)
         ↓
Method returns successfully
         ↓
@AfterReturning triggers PersistAsJsonAspect.persistAsJson()
         ↓
Aspect checks: jsonPersistenceEnabled?
         ↓ (yes)
Aspect extracts entity from return value or parameters
         ↓
For each entity:
  ├─ Check ID length >= 6? (skip if shorter)
  ├─ Calculate directory: /<type>/<char 0-2>/<char 3-5>/
  ├─ Create directories if needed
  ├─ Serialize entity to JSON with JsonMapper
  ├─ Write JSON to file: <id>.json
  └─ Log success (or error if fails, without throwing)
         ↓
Continue normal execution (no interruption on errors)
```

### Delete Flow

```
User calls service.delete(entityId)
         ↓
@Before triggers PersistAsJsonAspect.deleteEntityJson()
         ↓
Aspect checks: jsonPersistenceEnabled?
         ↓ (yes)
Aspect extracts entityId from parameters
         ↓
Check ID length >= 6? (skip if shorter)
         ↓
Calculate JSON file path
         ↓
If file exists:
  ├─ Delete JSON file
  ├─ Check if parent dir is empty → delete
  ├─ Check if grandparent dir is empty → delete
  └─ Log success (or error if fails, without throwing)
         ↓
Service method executes (deletes from DB)
         ↓
Continue normal execution
```

## Key Design Decisions

### 1. Minimal Invasiveness
- Only annotations added to service methods
- No changes to business logic
- Follows existing aspect patterns in the codebase

### 2. Error Handling
- All exceptions caught and logged in aspect
- Never interrupts database operations
- Ensures data consistency in primary storage

### 3. Configuration
- Feature disabled by default
- Must be explicitly enabled
- Can be toggled without code changes

### 4. ID Length Validation
- Requires minimum 6 characters
- Prevents substring errors
- Logs skipped entities at debug level

### 5. Directory Cleanup
- Removes empty directories after deletion
- Prevents cluttering file system
- Maintains clean directory structure

### 6. Timing
- Save: @AfterReturning (after successful DB save)
- Delete: @Before (before DB delete)
- Ensures JSON reflects actual DB state

## Testing Strategy

1. **Unit Tests** (`PersistAsJsonAspectTest`)
   - Mock dependencies
   - Test aspect pointcut matching
   - Verify JSON serialization
   - Test collection handling
   - Verify error handling

2. **Manual Integration Tests** (`PersistAsJsonAspectManualTest`)
   - Real file system operations
   - Test directory structure
   - Verify cleanup behavior
   - Test with actual domain entities

## Usage Example

```properties
# application.properties
artivact.json.persistence.enabled=true
```

After enabling, all entity saves/deletes automatically persist to JSON:

```java
// Save item
Item item = new Item();
item.setId("item12345678");
item.setTitle("My Item");
manageItemService.save(item);
// → Creates: projectRoot/items/ite/m12/item12345678.json

// Delete item
manageItemService.delete("item12345678");
// → Deletes: projectRoot/items/ite/m12/item12345678.json
// → Cleans up empty directories
```
