import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeItemDetailsPageScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/item-management/assets/item-details-page/',
        locale
    )

    await page.goto("http://localhost:9000/#/item/1916030e-c969-4001-97da-a52793491169", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="artivact-main-page"]');

    await takeScreenshot(
        page,
        '[data-test="artivact-main-page"]',
        outputDir,
        "item-details-page.png",
    );

    await takeScreenshot(
        page,
        '[data-test="delete-item-button"]',
        outputDir,
        "delete-item-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="download-item-button"]',
        outputDir,
        "download-item-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="sync-item-button"]',
        outputDir,
        "sync-item-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-item-button"]',
        outputDir,
        "edit-item-button.png",
    );

}