import { test, expect } from '@playwright/test';

test('application loads successfully', async ({ page }) => {
  await page.goto('/');

  // Wait for the main layout to appear
  const mainLayout = page.getByTestId('artivact-main-layout');
  await expect(mainLayout).toBeVisible({ timeout: 30_000 });
});

test('main page content area is visible', async ({ page }) => {
  await page.goto('/');

  // Wait for the main page content area to load
  const mainPage = page.getByTestId('artivact-main-page');
  await expect(mainPage).toBeVisible({ timeout: 30_000 });
});
