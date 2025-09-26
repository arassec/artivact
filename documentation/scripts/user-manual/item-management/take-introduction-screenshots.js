import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeItemManagementIntroductionScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/item-management/assets/introduction/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="artivact-main-layout"]');

    await takeScreenshot(
        page,
        '[data-test="item-settings-button"]',
        outputDir,
        "item-settings-button.png",
    );

    await page.waitForSelector('[data-test="item-settings-button"]');

    await page.$eval('[data-test="item-settings-button"]', (element) =>
        element.click(),
    );

    await new Promise((r) => setTimeout(r, 1000));

    await takeScreenshot(
        page,
        '[data-test="item-settings-menu"]',
        outputDir,
        "item-settings-menu.png",
    );

}