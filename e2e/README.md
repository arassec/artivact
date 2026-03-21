# Artivact E2E Tests

End-to-end tests for the Artivact application using [Playwright](https://playwright.dev/).

## Overview

This module runs Playwright E2E tests against the full Artivact application (Spring Boot backend serving
the Vue.js frontend). The backend is started automatically with the `e2e` Spring profile, which:

- Auto-authenticates as an admin user (no login required).
- Imports a default welcome page for testing.
- Disables CSRF protection.

## Running E2E Tests

### As Part of the Maven Build

E2E tests are run as part of an optional Maven profile. By default, the `e2e` module is **not** included
in the standard build, which allows `mvn clean install` to run on any operating system without requiring
Ubuntu-specific browser dependency tooling.

#### On Ubuntu / Debian (CI and local)

To run E2E tests including automatic OS-level dependency installation:

```bash
./mvnw clean install -Pe2e -Dplaywright.deps.phase=generate-test-sources
```

#### On Fedora, Windows, or other non-Ubuntu systems

To run E2E tests without attempting to install OS-level browser dependencies
(system Chromium libraries must already be present):

```bash
./mvnw clean install -Pe2e
```

The Maven build will:
1. Install Node.js and npm (via `frontend-maven-plugin`).
2. Install npm dependencies.
3. Install Playwright Chromium browser binary.
4. (Ubuntu only, when `playwright.deps.phase` is set) Install OS-level Chromium dependencies.
5. Start the Artivact server with the `e2e` profile.
6. Run Playwright tests against the running server.
7. Stop the server after tests complete.

### Running Tests Locally (Without Maven)

If you want to iterate on tests without a full Maven build:

1. **Build the server JAR first** (from project root):
   ```bash
   ./mvnw clean install -pl artivact-server -am
   ```

2. **Install dependencies** (from the `e2e` directory):
   ```bash
   npm install
   npx playwright install chromium
   # On Ubuntu/Debian, additionally install OS-level browser dependencies:
   npx playwright install-deps chromium
   ```

3. **Run tests**:
   ```bash
   npx playwright test
   ```
   This will automatically start the server, run tests, and stop the server.

4. **Run tests with UI mode** (for debugging):
   ```bash
   npx playwright test --ui
   ```

5. **Run against an already running server**:
   Start the server manually (replace the version as needed):
   ```bash
   java -jar -Dspring.profiles.active=e2e ../artivact-server/target/artivact-server-*.jar
   ```
   Then run tests (Playwright will reuse the existing server when not in CI):
   ```bash
   npx playwright test
   ```

## CI Integration

In GitHub Actions, the E2E tests are part of the Maven build on Linux runners. The `e2e` Maven profile
is activated and the `playwright.deps.phase` property is set to ensure OS-level Chromium dependencies
are installed on Ubuntu. Windows CI builds skip the E2E tests.

The Playwright configuration uses:
- `forbidOnly: true` in CI to prevent accidental `.only` tests.
- `retries: 1` in CI for flaky test resilience.
- `reuseExistingServer: false` in CI to ensure a fresh server for each run.

## Configuration

Key files:
- `playwright.config.ts` — Playwright configuration (base URL, browser, server startup).
- `tests/` — Test files (TypeScript).
- `pom.xml` — Maven integration via `frontend-maven-plugin`.

### Environment Variables

| Variable             | Description                          | Default                                                            |
|----------------------|--------------------------------------|--------------------------------------------------------------------|
| `SERVER_JAR`         | Path to the Artivact server JAR      | Auto-detected from `../artivact-server/target/artivact-server-*.jar` |
| `ARTIVACT_DATA_DIR`  | Directory for application data       | `./e2e/target/avdata`                                              |
| `CI`                 | Set automatically in CI environments | —                                                                  |

## Writing Tests

Tests use Playwright's test runner with TypeScript. The frontend uses `data-test` attributes for test
selectors (configured via `testIdAttribute` in the Playwright config).

Example:
```typescript
import { test, expect } from '@playwright/test';

test('example test', async ({ page }) => {
  await page.goto('/');
  const element = page.getByTestId('artivact-main-layout');
  await expect(element).toBeVisible();
});
```

## Test Reports

After running tests, HTML reports are generated in `target/playwright-report/`. Open the report with:

```bash
npx playwright show-report target/playwright-report
```
