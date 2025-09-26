// scripts/screenshot.js
import puppeteer from "puppeteer";

import {takeContentManagementPagesScreenshots} from "./user-manual/content-management/take-pages-screenshots.js";
import {takeSettingsIntroductionScreenshots} from "./user-manual/settings/take-settings-introduction-screenshots.js";
import {takeSettingsPeripheralsScreenshots} from "./user-manual/settings/take-settings-peripherals-screenshots.js";
import {takeContentManagementMenusScreenshots} from "./user-manual/content-management/take-menus-screenshots.js";
import {takeIntroductionScreenshots} from "./user-manual/introduction/take-introduction-screenshots.js";
import {
    takeItemManagementIntroductionScreenshots
} from "./user-manual/item-management/take-introduction-screenshots.js";
import {takeItemEditorScreenshots} from "./user-manual/item-management/take-item-editor-screenshots.js";
import {takeItemDetailsPageScreenshots} from "./user-manual/item-management/take-item-details-page-screenshots.js";

async function run() {
    await createScreenshots("en");
    await createScreenshots("de");
}

async function createScreenshots(locale) {
    const url = 'http://localhost:8080/api/locale/' + locale;
    const res = await fetch(url, {method: 'GET'});
    if (res.status !== 200) {
        console.log(`Error application switching locale: ${res.status}`);
    }

    const browser = await puppeteer.launch({
        headless: false,
        args: [`--lang=${locale}-${locale.toUpperCase()}`],
        defaultViewport: {
            width: 1280,
            height: 1250
        }
    });

    const page = await browser.newPage();
    await page.setViewport({width: 1280, height: 1250});

    await page.setExtraHTTPHeaders({
        'Accept-Language': `${locale}-${locale.toUpperCase()},${locale};q=0.9,en-US;q=0.8,en;q=0.7`
    })

    await page.evaluateOnNewDocument((loc) => {
        Object.defineProperty(navigator, "language", {
            get: () => `${loc}-${loc.toUpperCase()}`,
            configurable: true
        })
        Object.defineProperty(navigator, "languages", {
            get: () => [`${loc}-${loc.toUpperCase()}`, loc],
            configurable: true
        })
    }, locale)

    await takeIntroductionScreenshots(page, locale);
    await takeItemManagementIntroductionScreenshots(page, locale);
    await takeItemEditorScreenshots(page, locale);
    await takeItemDetailsPageScreenshots(page, locale);
    await takeContentManagementMenusScreenshots(page, locale);
    await takeContentManagementPagesScreenshots(page, locale);
    await takeSettingsIntroductionScreenshots(page, locale);
    await takeSettingsPeripheralsScreenshots(page, locale);

    await browser.close();
}

run();
