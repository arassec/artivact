import { test, expect, Page } from '@playwright/test';
import { writeFileSync } from 'fs';
import { join } from 'path';
import { tmpdir } from 'os';

const PAGE_LOAD_TIMEOUT = 30_000;

async function navigateToTagsPage(page: Page) {
  await page.goto('/');
  await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  await page.getByTestId('system-settings-button').click();
  await expect(page.getByTestId('system-settings-menu')).toBeVisible();
  await page.getByTestId('artivact-system-settings-tags').click();
  await page.waitForURL('**/administration/configuration/tags');
}

async function resetTagsConfiguration(page: Page) {
  await page.goto('/');
  await expect(page.getByTestId('artivact-main-layout')).toBeVisible({ timeout: PAGE_LOAD_TIMEOUT });
  await page.evaluate(async () => {
    await fetch('/api/configuration/tags', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ tags: [] }),
    });
  });
}

// ─── Navigation & Rendering ─────────────────────────────────────────────────

test.describe('Tags Configuration Page - Navigation & Rendering', () => {

  test.beforeEach(async ({ page }) => {
    await navigateToTagsPage(page);
  });

  test('page loads and shows heading', async ({ page }) => {
    await expect(page.getByTestId('tags-configuration-heading')).toBeVisible();
    await expect(page.getByTestId('tags-configuration-heading')).toContainText('Tags');
  });

  test('default tab is configuration', async ({ page }) => {
    await expect(page.getByTestId('tags-configuration-tab')).toHaveClass(/q-tab--active/);
  });

  test('save button is visible on configuration tab', async ({ page }) => {
    await expect(page.getByTestId('save-tags-button')).toBeVisible();
  });

  test('add tag button is visible on configuration tab', async ({ page }) => {
    await expect(page.getByTestId('add-tag-button')).toBeVisible();
  });
});

// ─── Tab Switching ───────────────────────────────────────────────────────────

test.describe('Tags Configuration Page - Tab Switching', () => {

  test.beforeEach(async ({ page }) => {
    await navigateToTagsPage(page);
  });

  test('switching to export tab shows download button', async ({ page }) => {
    await page.getByTestId('tags-export-tab').click();
    await expect(page.getByTestId('export-tags-button')).toBeVisible();
  });

  test('switching to import tab shows uploader', async ({ page }) => {
    await page.getByTestId('tags-import-tab').click();
    await expect(page.getByTestId('import-tags-uploader')).toBeVisible();
  });

  test('switching back to configuration tab shows editor', async ({ page }) => {
    await page.getByTestId('tags-export-tab').click();
    await expect(page.getByTestId('export-tags-button')).toBeVisible();

    await page.getByTestId('tags-configuration-tab').click();
    await expect(page.getByTestId('save-tags-button')).toBeVisible();
    await expect(page.getByTestId('add-tag-button')).toBeVisible();
  });

  test('import uploader accepts .tags.artivact.configuration.json files', async ({ page }) => {
    await page.getByTestId('tags-import-tab').click();
    const uploader = page.getByTestId('import-tags-uploader');
    await expect(uploader).toBeVisible();
    await expect(uploader.locator('input[type="file"]')).toHaveAttribute(
      'accept',
      '.tags.artivact.configuration.json'
    );
  });
});

// ─── Configuration Editor CRUD ───────────────────────────────────────────────

test.describe('Tags Configuration Page - CRUD', () => {

  test.beforeEach(async ({ page }) => {
    await resetTagsConfiguration(page);
    await page.getByTestId('system-settings-button').click();
    await expect(page.getByTestId('system-settings-menu')).toBeVisible();
    await page.getByTestId('artivact-system-settings-tags').click();
    await page.waitForURL('**/administration/configuration/tags');
  });

  test('shows no-tags-defined hint when no tags exist', async ({ page }) => {
    await expect(page.getByTestId('tags-no-tags-hint')).toBeVisible();
    await expect(page.getByTestId('tags-no-tags-hint')).toContainText(
      'no tags defined'
    );
  });

  test('no expansion items are rendered when no tags exist', async ({ page }) => {
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(0);
  });

  test('add a tag creates an expansion item', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();

    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);
    // The no-tags-defined hint should disappear
    await expect(page.getByTestId('tags-no-tags-hint')).not.toBeVisible();
  });

  test('added tag has default label "New Tag"', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();

    const expansionItem = page.getByTestId('tag-expansion-item').first();
    await expect(expansionItem).toContainText('New Tag');
  });

  test('add tag, save, and reload persists the tag', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);

    // Save
    const saveResponse = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/tags') && resp.request().method() === 'POST'
    );
    await page.getByTestId('save-tags-button').click();
    await saveResponse;

    // Reload the page and verify tag persists
    await navigateToTagsPage(page);
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);
  });

  test('delete a tag removes the expansion item', async ({ page }) => {
    // Add two tags
    await page.getByTestId('add-tag-button').click();
    await page.getByTestId('add-tag-button').click();
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(2);

    // Delete the first tag
    await page.getByTestId('delete-tag-button').first().click();
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);
  });

  test('delete tag, save, and reload confirms removal', async ({ page }) => {
    // Add a tag and save
    await page.getByTestId('add-tag-button').click();
    const saveResponse1 = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/tags') && resp.request().method() === 'POST'
    );
    await page.getByTestId('save-tags-button').click();
    await saveResponse1;

    // Reload to confirm it exists
    await navigateToTagsPage(page);
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);

    // Delete the tag
    await page.getByTestId('delete-tag-button').first().click();
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(0);

    // Save
    const saveResponse2 = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/tags') && resp.request().method() === 'POST'
    );
    await page.getByTestId('save-tags-button').click();
    await saveResponse2;

    // Reload and verify tag is gone
    await navigateToTagsPage(page);
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(0);
    await expect(page.getByTestId('tags-no-tags-hint')).toBeVisible();
  });

  test('expanding a tag shows url input and default checkbox', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();

    // Click on the expansion item header to expand it
    await page.getByTestId('tag-expansion-item').first().click();

    await expect(page.getByTestId('tag-url-input')).toBeVisible();
    await expect(page.getByTestId('tag-default-checkbox')).toBeVisible();
  });

  test('can edit tag url and toggle default checkbox', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();
    await page.getByTestId('tag-expansion-item').first().click();

    // Fill in the URL
    const urlInput = page.getByTestId('tag-url-input');
    await urlInput.fill('https://example.com/tag');

    // Toggle the default checkbox
    await page.getByTestId('tag-default-checkbox').click();

    // Save
    const saveResponse = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/tags') && resp.request().method() === 'POST'
    );
    await page.getByTestId('save-tags-button').click();
    await saveResponse;

    // Reload and verify
    await navigateToTagsPage(page);
    await page.getByTestId('tag-expansion-item').first().click();
    await expect(page.getByTestId('tag-url-input')).toHaveValue('https://example.com/tag');
  });

  test('adding multiple tags creates multiple expansion items', async ({ page }) => {
    await page.getByTestId('add-tag-button').click();
    await page.getByTestId('add-tag-button').click();
    await page.getByTestId('add-tag-button').click();

    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(3);
  });

  test('save shows success notification', async ({ page }) => {
    const saveResponse = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/tags') && resp.request().method() === 'POST'
    );
    await page.getByTestId('save-tags-button').click();
    await saveResponse;

    // Verify success notification appears
    await expect(page.getByText('Tags configuration saved')).toBeVisible({ timeout: 5_000 });
  });
});

// ─── Export Function ─────────────────────────────────────────────────────────

test.describe('Tags Configuration Page - Export', () => {

  test.beforeEach(async ({ page }) => {
    await navigateToTagsPage(page);
  });

  test('export button triggers file download', async ({ page }) => {
    await page.getByTestId('tags-export-tab').click();

    const downloadPromise = page.waitForEvent('download');
    await page.getByTestId('export-tags-button').click();
    const download = await downloadPromise;

    expect(download.suggestedFilename()).toMatch(/\.tags\.artivact\.configuration\.json$/);
  });
});

// ─── Import Function ─────────────────────────────────────────────────────────

test.describe('Tags Configuration Page - Import', () => {

  test.beforeEach(async ({ page }) => {
    await resetTagsConfiguration(page);
    await page.getByTestId('system-settings-button').click();
    await expect(page.getByTestId('system-settings-menu')).toBeVisible();
    await page.getByTestId('artivact-system-settings-tags').click();
    await page.waitForURL('**/administration/configuration/tags');
  });

  test('upload a valid tags configuration file updates the tag list', async ({ page }) => {
    // First confirm no tags exist
    await expect(page.getByTestId('tags-no-tags-hint')).toBeVisible();

    // Create a temporary valid tags configuration file
    const tagsConfig = {
      tags: [
        {
          id: 'imported-tag-1',
          value: 'Imported Tag',
          translations: {},
          restrictions: [],
          translatedValue: '',
          url: 'https://example.com',
          defaultTag: false,
        },
      ],
    };
    const filePath = join(tmpdir(), 'test.tags.artivact.configuration.json');
    writeFileSync(filePath, JSON.stringify(tagsConfig));

    // Switch to import tab
    await page.getByTestId('tags-import-tab').click();
    const uploader = page.getByTestId('import-tags-uploader');
    await expect(uploader).toBeVisible();

    // Upload the file
    const fileInput = uploader.locator('input[type="file"]');
    const uploadResponse = page.waitForResponse(
      (resp) => resp.url().includes('/api/configuration/import/tags')
    );
    await fileInput.setInputFiles(filePath);
    await uploadResponse;

    // Verify success notification
    await expect(page.getByText('Tags configuration uploaded')).toBeVisible({ timeout: 5_000 });

    // Switch back to configuration tab and verify the imported tag
    await page.getByTestId('tags-configuration-tab').click();
    await expect(page.getByTestId('tag-expansion-item')).toHaveCount(1);
    await expect(page.getByTestId('tags-no-tags-hint')).not.toBeVisible();
  });
});
