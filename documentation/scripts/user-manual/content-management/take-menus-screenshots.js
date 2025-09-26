import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeContentManagementMenusScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/content-management/assets/menus/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="artivact-main-layout"]');

    await takeScreenshot(
        page,
        '[data-test="add-menu-button"]',
        outputDir,
        "add-menu-button.png",
    );

    await page.$eval('[data-test="add-menu-button"]', (element) =>
        element.click(),
    );

    await new Promise((r) => setTimeout(r, 1000));

    await takeScreenshot(
        page,
        '[data-test="add-menu-menu"]',
        outputDir,
        "add-menu-menu.png",
    );

}