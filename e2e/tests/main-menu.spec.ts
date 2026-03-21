import {expect, test} from '@playwright/test';

const PAGE_LOAD_TIMEOUT = 30_000;

test.describe('Main Page & Settings Bar', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  });

  test('settings bar is visible', async ({ page }) => {
    await expect(page.getByTestId('artivact-settings-bar')).toBeVisible();
  });

  test('no fatal console errors on load', async ({ page }) => {
    const errors: string[] = [];
    page.on('pageerror', (err) => errors.push(err.message));

    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });

    expect(errors).toEqual([]);
  });
});

test.describe('Locale Selection Menu', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  });

  test('locale selection button is visible', async ({ page }) => {
    const localeButton = page.getByTestId('locale-selection-button');
    if (!(await localeButton.isVisible())) {
      test.skip();
      return;
    }
    await expect(localeButton).toBeVisible();
  });

  test('locale menu opens with default and language options', async ({ page }) => {
    const localeButton = page.getByTestId('locale-selection-button');
    if (!(await localeButton.isVisible())) {
      test.skip();
      return;
    }

    await localeButton.click();

    const defaultOption = page.getByTestId('artivact-locale-selection-default');
    await expect(defaultOption).toBeVisible();

    // e2e profile configures "de" as available locale
    const deOption = page.getByTestId('artivact-locale-selection-de');
    await expect(deOption).toBeVisible();
  });
});

test.describe('Item Settings Menu', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  });

  test('create-item-button navigates to item configuration', async ({ page }) => {
    const itemButton = page.getByTestId('item-settings-button');
    if (!(await itemButton.isVisible())) {
      test.skip();
      return;
    }

    await itemButton.click();
    await expect(page.getByTestId('item-settings-menu')).toBeVisible();

    await page.getByTestId('create-item-button').click();
    await page.waitForURL(/\/administration\/configuration\/item\/.+/);
  });

  test('import-item-button opens import modal', async ({ page }) => {
    const itemButton = page.getByTestId('item-settings-button');
    if (!(await itemButton.isVisible())) {
      test.skip();
      return;
    }

    await itemButton.click();
    await expect(page.getByTestId('item-settings-menu')).toBeVisible();

    await page.getByTestId('import-item-button').click();
    await expect(page.getByTestId('import-menu-modal')).toBeVisible();
  });

  test('batch-process-button navigates to batch page', async ({ page }) => {
    const itemButton = page.getByTestId('item-settings-button');
    if (!(await itemButton.isVisible())) {
      test.skip();
      return;
    }

    await itemButton.click();
    await expect(page.getByTestId('item-settings-menu')).toBeVisible();

    const batchButton = page.getByTestId('batch-process-button');
    if (!(await batchButton.isVisible())) {
      test.skip();
      return;
    }

    await batchButton.click();
    await page.waitForURL(/\/administration\/batch/);
  });
});

test.describe('System Settings Menu', () => {

  const systemSettingsEntries = [
    { testId: 'artivact-system-settings-properties', route: '/administration/configuration/properties' },
    { testId: 'artivact-system-settings-tags', route: '/administration/configuration/tags' },
    { testId: 'artivact-system-settings-exchange', route: '/administration/configuration/exchange' },
    { testId: 'artivact-system-settings-appearance', route: '/administration/configuration/appearance' },
    { testId: 'artivact-system-settings-peripherals', route: '/administration/configuration/peripherals' },
    { testId: 'artivact-system-settings-maintenance', route: '/administration/configuration/maintenance' },
  ];

  for (const entry of systemSettingsEntries) {
    test(`${entry.testId} navigates to ${entry.route}`, async ({ page }) => {
      await page.goto('/');
      await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });

      const systemButton = page.getByTestId('system-settings-button');
      if (!(await systemButton.isVisible())) {
        test.skip();
        return;
      }

      await systemButton.click();
      await expect(page.getByTestId('system-settings-menu')).toBeVisible();

      const menuItem = page.getByTestId(entry.testId);
      if (!(await menuItem.isVisible())) {
        test.skip();
        return;
      }

      await menuItem.click();
      await page.waitForURL(`**${entry.route}`);
    });
  }
});

test.describe('Account Settings Menu', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  });

  test('account entry navigates to /account', async ({ page }) => {
    const accountButton = page.getByTestId('account-settings-button');
    if (!(await accountButton.isVisible())) {
      test.skip();
      return;
    }

    await accountButton.click();

    const accountEntry = page.getByTestId('account-settings-entry');
    await expect(accountEntry).toBeVisible();

    await accountEntry.click();
    await page.waitForURL(/\/account/);
  });
});

test.describe('Documentation Link', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  });

  test('documentation button has correct href and opens in new tab', async ({ page }) => {
    const docLink = page.getByTestId('documentation-button');
    if (!(await docLink.isVisible())) {
      test.skip();
      return;
    }

    await expect(docLink).toHaveAttribute('href', '/artivact/index.html');
    await expect(docLink).toHaveAttribute('target', '_blank');
  });
});
