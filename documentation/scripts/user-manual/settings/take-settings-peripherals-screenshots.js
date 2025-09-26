import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeSettingsPeripheralsScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/settings/assets/peripherals/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.$eval('[data-test="system-settings-button"]', (element) =>
        element.click(),
    );

    await page.waitForSelector('[data-test="artivact-system-settings-peripherals"]');

    await page.$eval('[data-test="artivact-system-settings-peripherals"]', (element) =>
        element.click(),
    );

    await new Promise((r) => setTimeout(r, 1000));

    await takeScreenshot(
        page,
        '[data-test="peripherals-configuration"]',
        outputDir,
        "peripherals-configuration.png",
    );

}
