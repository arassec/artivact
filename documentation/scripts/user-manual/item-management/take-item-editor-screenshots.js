import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeItemEditorScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/item-management/assets/item-editor/',
        locale
    )

    await page.goto("http://localhost:9000/#/item/1916030e-c969-4001-97da-a52793491169", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="edit-item-button"]');

    await takeScreenshot(
        page,
        '[data-test="edit-item-button"]',
        outputDir,
        "edit-item-button.png",
    );

    await page.$eval('[data-test="edit-item-button"]', (element) =>
        element.click(),
    );

    await page.waitForSelector('[data-test="close-button"]', {visible: true});

    await takeScreenshot(
        page,
        '[data-test="close-button"]',
        outputDir,
        "close-item-editor-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-item-base-tab"]',
        outputDir,
        "edit-item-base-tab.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-item-media-tab"]',
        outputDir,
        "edit-item-media-tab.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-item-properties-tab"]',
        outputDir,
        "edit-item-properties-tab.png",
    );

    await takeScreenshot(
        page,
        '[data-test="edit-item-creation-tab"]',
        outputDir,
        "edit-item-creation-tab.png",
    );

    await page.$eval('[data-test="edit-item-creation-tab"]', (element) =>
        element.click(),
    );

    await page.waitForSelector('[data-test="item-creation-image-button"]', {visible: true});

    await takeScreenshot(
        page,
        '[data-test="item-creation-image-button"]',
        outputDir,
        "item-creation-image-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="item-creation-camera-button"]',
        outputDir,
        "item-creation-camera-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="item-creation-create-model-button"]',
        outputDir,
        "item-creation-create-model-button.png",
    );

}