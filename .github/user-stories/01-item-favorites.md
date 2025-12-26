# User Story: Copy & Paste Properties from Favorites

**As a** user,
**I want to** copy the properties of a saved favorite and paste them into the object I am currently editing,
**So that** I can quickly apply reusable property sets without manually re-entering them.

## Acceptance Criteria

- **Copy Action:**
  - A "Copy Properties" button is added to the Favorites dropdown list.
  - The button is positioned to the left of the "Remove Favorite" button.
  - Clicking the button saves the properties of the selected favorite into a temporary client-side store.
  - A success notification (Toast) confirms that properties have been copied.
  - The store only holds the most recently copied set of properties (overwrites previous).

- **Paste Action:**
  - A "Paste Properties" button is added to the Properties tab/editor.
  - The button is positioned to the left of the "Exit Edit Mode" button.
  - Clicking the button applies the stored properties to the currently edited object.
  - A success notification (Toast) confirms that properties have been pasted.

- **Technical Constraints:**
  - Purely frontend implementation (no backend changes).
  - State management via Pinia.

---

## Implementation Guide for AI Agent

### 1. State Management (Pinia)

- **File:** Locate the Pinia store responsible for favorites (e.g., `src/stores/favorites.store.ts`).
- **State:** Add a state property to hold the copied properties (e.g., `copiedProperties: Property[] | null`).
- **Actions:**
  - `copyProperties(properties: Property[])`: Sets the state.
  - `getCopiedProperties()`: Returns the current state.
- **Type Safety:** Ensure strict TypeScript typing for the `Property` interface.

### 2. Component: Favorites Dropdown

- **File:** Locate the component rendering the favorites list (e.g., `FavoritesMenu.vue` or similar).
- **UI:** Add a `<q-btn>` with an icon (e.g., `content_copy` or `file_copy`) inside the list item or dropdown action
  area.
- **Logic:**
  - On click, invoke the store's `copyProperties` action.
  - Trigger a Quasar Notification:
    ```typescript
    $q.notify({
      type: 'positive',
      message: 'Properties copied to clipboard'
    })
    ```

### 3. Component: Properties Editor

- **File:** Locate the component where properties are edited (e.g., `PropertiesTab.vue` or `ItemDetails.vue`).
- **UI:** Add a `<q-btn>` with an icon (e.g., `content_paste`) next to the "Exit Edit Mode" button.
- **Logic:**
  - On click, retrieve data from the store.
  - Merge the retrieved properties into the local form model of the object being edited.
  - Trigger a Quasar Notification:
    ```typescript
    $q.notify({
      type: 'positive',
      message: 'Properties pasted successfully'
    })
    ```
  - **Validation:** Ensure the button is disabled or handles the case if the store is empty.

### 4. General Guidelines

- Use **Vue 3 Composition API** (`<script setup>`).
- Use **Quasar** components (`QBtn`, `QIcon`, `QItem`, etc.).
- Ensure all text is in English.
- Follow the project's Hexagonal Architecture: Keep this logic within the **Frontend Module**.
