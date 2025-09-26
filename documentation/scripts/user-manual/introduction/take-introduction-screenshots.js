import {createTargetDir, takeScreenshot} from "../../utils.js";
import {fileURLToPath} from "url";
import {dirname} from "path";

export async function takeIntroductionScreenshots(page, locale) {
    const outputDir = await createTargetDir(
        dirname(fileURLToPath(import.meta.url)),
        '/user-manual/introduction/assets/about/',
        locale
    )

    await page.goto("http://localhost:9000/", {waitUntil: "networkidle0"});

    await page.waitForSelector('[data-test="artivact-main-layout"]');

    await takeScreenshot(
        page,
        '[data-test="artivact-main-layout"]',
        outputDir,
        "artivact-welcome-page.png",
    );

    await takeScreenshot(
        page,
        '[data-test="add-menu-button"]',
        outputDir,
        "add-menu-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="locale-selection-button"]',
        outputDir,
        "locale-selection-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="item-settings-button"]',
        outputDir,
        "item-settings-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="system-settings-button"]',
        outputDir,
        "system-settings-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="account-settings-button"]',
        outputDir,
        "account-settings-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="documentation-button"]',
        outputDir,
        "documentation-button.png",
    );

    await takeScreenshot(
        page,
        '[data-test="logout-button"]',
        outputDir,
        "logout-button.png",
    );

    await page.$eval('[data-test="logout-button"]', (element) =>
        element.click(),
    );

    await page.waitForSelector('[data-test="login-button"]', {visible: true});

    await takeScreenshot(
        page,
        '[data-test="login-button"]',
        outputDir,
        "login-button.png",
    );

}
