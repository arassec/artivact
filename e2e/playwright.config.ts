import { defineConfig, devices } from '@playwright/test';
import { readdirSync } from 'fs';
import { join } from 'path';

function findServerJar(): string {
  const targetDir = join(__dirname, '..', 'artivact-server', 'target');
  try {
    const files = readdirSync(targetDir);
    const jar = files.find(f => f.startsWith('artivact-server-') && f.endsWith('.jar'));
    if (jar) return join(targetDir, jar);
  } catch { /* target dir may not exist yet */ }
  return join(targetDir, 'artivact-server.jar');
}

const serverJar = process.env.SERVER_JAR || findServerJar();

const dataDir = process.env.ARTIVACT_DATA_DIR || './e2e/target/avdata';

export default defineConfig({
  testDir: './tests',
  fullyParallel: false,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [['html', { open: 'never', outputFolder: 'target/playwright-report' }]],

  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
    testIdAttribute: 'data-test',
  },

  outputDir: 'target/test-results',

  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],

  webServer: {
    command: `java -jar -Dspring.profiles.active=e2e -Dartivact.project.root=${dataDir} ${serverJar}`,
    url: 'http://localhost:8080',
    reuseExistingServer: !process.env.CI,
    timeout: 120_000,
    cwd: '..',
  },
});
