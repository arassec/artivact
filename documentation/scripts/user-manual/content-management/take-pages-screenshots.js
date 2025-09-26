import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeContentManagementPagesScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/content-management/assets/pages/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="artivact-main-layout"]');

    await page.$eval('[data-test="edit-page-button"]', (element) =>
        element.click(),
    );

    await page.waitForSelector('[data-test="close-button"]');

    await takeScreenshot(
        page,
        '[data-test="reset-wip-button"]',
        outputDir,
        "reset-wip-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="publish-wip-button"]',
        outputDir,
        "publish-wip-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-metadata-button"]',
        outputDir,
        "edit-metadata-button.png",
    );

}