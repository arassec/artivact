# General Idea

Users can mark items as favorites to easily access them later. This feature enhances user experience by allowing quick
retrieval of frequently used or preferred items.

# Use Case: Item Favorites

## Story

As user i want to mark items as favorites so that I can easily access them later.

## Functional Requirements (Agent-ready)

### FR-01: Mark / unmark item as favorite

- The system shall allow an authenticated user to mark any existing item as favorite.
- The system shall allow the same user to remove the favorite mark from an item.
- Marking an already-favorited item as favorite again shall not create duplicates.
- Unmarking an item that is not in the favorites list shall be a no-op and must not fail.

**UI specifics**

- A star-button in the top right corner of the item details page can be clicked to mark/unmark the item as a favorite.
- The button is only shown when the user is logged in.
- A filled star indicates that the item is marked as a favorite, while an empty star indicates that it is not.
- The star-button must reflect the current favorite state when the item details page is loaded (no intermediate flicker
  from default state).

**Backend / API hints (for implementation agents)**

- Introduce an authenticated REST API (e.g. under `/api/favorites`):
    - `POST /api/favorites/{itemId}` → mark item as favorite for current user.
    - `DELETE /api/favorites/{itemId}` → remove item from favorites for current user.
    - `GET /api/favorites/{itemId}` → returns whether the item is currently a favorite for the user (boolean).
- The API must be tied to the authenticated user (no user id in the URL; taken from security context).
- All operations must be idempotent for a given user+item combination.

### FR-02: List user favorites

- Users can view a list of their favorite items in a dedicated "Favorites" menu in the top navigation bar.
- The "Favorites" menu displays all items marked as favorites in a drop-down list, allowing users to quickly access
  them.
- A click on a favorite item in the list navigates the user to the item's details page.
- Users can remove items from their favorites list by clicking the star-button again on the item's details or by
  clicking a remove icon next to the item in the "Favorites" menu.

**Backend / API hints (for implementation agents)**

- Provide an endpoint, authenticated only:
    - `GET /api/favorites` → returns a list of favorite items for the current user.
- The response shall contain, at minimum, for each favorite:
    - `itemId` (string or UUID, depending on existing model),
    - `title` (string),
    - optionally additional lightweight fields needed for the dropdown (e.g. thumbnail URL, short description).
- Ordering:
    - Default ordering is “most recently favorited first”.
    - If a favorite is removed and re-added, it should appear as latest again.
- Pagination:
    - If needed, support pagination via query parameters (`page`, `size`) with reasonable defaults.

### FR-03: Persistence and user session behavior

- The favorites list is persistent across user sessions, meaning that items marked as favorites remain so even after
  logging out and back in.
- Favorites are scoped per user: each user has their own independent favorites list.
- If an item is deleted from the system:
    - It must be automatically removed from all favorites lists, or at least not break favorites listing APIs.
    - Listing favorites must not fail because of deleted items; such entries must be skipped or cleaned up.

**Persistence hints (for implementation agents)**

- Use a dedicated favorites persistence concept (table / collection), e.g.:
    - `user_favorites (user_id, item_id, created_at)` with `(user_id, item_id)` as unique key.
- Ensure that:
    - Queries are efficient for “all favorites of user” (index on `user_id`).
    - Optional: cascading cleanup or scheduled cleanup for favorites pointing to deleted items.

## Non-Functional Requirements (Agent-ready)

### NFR-01: Security & Access control

- Only authenticated users can:
    - Mark or unmark favorites.
    - Retrieve their favorites list.
    - Query favorite status of an item for themselves.
- No user must be able to manipulate or read another user’s favorites.
- If a request is sent without valid authentication, the API must respond with `401 Unauthorized`.

### NFR-02: Performance

- Mark/unmark operations must complete within typical API latency (aim: < 200 ms under normal load).
- Listing favorites for a typical user (up to 100 favorites) must complete within < 300 ms under normal load.

### NFR-03: UX behavior

- The UI shall optimistically update the star icon state on click and then sync with the backend result.
- In case of a backend error, the UI shall revert the star state and show a non-intrusive error message.

## Domain & Architecture Notes (for implementation agents)

- Follow the existing Ports and Adapters architecture:
    - **Domain**: Introduce or reuse domain concepts for user identity and item identity. A favorite can be represented
      as a value object combining `UserId` and `ItemId`.
    - **Application**:
        - Define inbound ports (use cases), e.g.:
            - `MarkItemAsFavoriteUseCase`
            - `UnmarkItemAsFavoriteUseCase`
            - `ListFavoriteItemsUseCase`
            - `IsItemFavoriteUseCase`
        - Define outbound ports for:
            - Accessing favorites persistence (`FavoriteRepositoryPort`).
            - Resolving item details needed for the favorites dropdown (`ItemQueryPort`).
    - **Adapters**:
        - Implement REST controllers that map HTTP endpoints to the application use cases.
        - Implement persistence adapters (e.g. JPA repositories) for storing and fetching favorites.
        - Implement frontend integration:
            - Star button component in item details view.
            - "Favorites" menu component in top navigation bar.
- All new code (domain, application, adapters, frontend) must use English identifiers and comments.

## Acceptance Criteria (Agent checklist)

1. When a logged-in user opens an item details page:
    - The star-button is visible.
    - The star state (filled/empty) matches the backend favorite status.
2. When the user clicks the star-button:
    - The UI toggles the star state.
    - A request is sent to the backend to mark/unmark the item.
    - After a successful response, the favorites list in the navigation is updated accordingly.
3. When the user opens the "Favorites" menu:
    - The user sees only their own favorite items.
    - Clicking on an item navigates to that item’s details page.
4. After logging out and logging in again with the same account:
    - Previously favorited items still appear as favorites (star filled, listed in favorites menu).
5. When an item is deleted from the system:
    - It no longer appears in any user's favorites list.
    - Listing favorites for a user who had this item does not fail.
