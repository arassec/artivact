import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeSettingsIntroductionScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/settings/assets/introduction/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="system-settings-button"]');

    await takeScreenshot(
        page,
        '[data-test="system-settings-button"]',
        outputDir,
        "system-settings-button.png",
    );

    await page.$eval('[data-test="system-settings-button"]', (element) =>
        element.click(),
    );

    await new Promise((r) => setTimeout(r, 1000));

    await takeScreenshot(
        page,
        '[data-test="system-settings-menu"]',
        outputDir,
        "settings-menu.png",
    );

}
