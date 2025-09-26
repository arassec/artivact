import {resolve} from "path";
import fs from "fs";

export const TARGET_DIR_PREFIX = '../../../docs';

/**
 * @param {import("puppeteer").Page} page - Puppeteer Page instance
 */
export async function logDataTestAttributes(page) {
    const attributes = await page.$$eval('[data-test]', els =>
        els.map(el => ({
            tag: el.tagName.toLowerCase(),
            dataTest: el.getAttribute('data-test'),
            text: el.textContent.trim().slice(0, 50) // bisschen Kontext
        }))
    );

    console.log("Found 'data-test' attributes:");
    for (const attr of attributes) {
        console.log(`- <${attr.tag}> [data-test="${attr.dataTest}"] => "${attr.text}"`);
    }
}

export async function takeScreenshot(page, selector, outputDir, filename) {
    const element = await page.$(selector);
    if (element) {
        await element.screenshot({path: `${outputDir}/${filename}`});
    } else {
        console.error(`Element ${selector} not found!`);
    }
}

export async function createTargetDir(scriptDir, targetDirSuffix, locale) {
    let targetDir = TARGET_DIR_PREFIX;
    if (locale !== 'en') {
        targetDir = targetDir + '/' + locale;
    }
    targetDir = `${targetDir}/${targetDirSuffix}`;
    const result = resolve(scriptDir, targetDir);
    fs.mkdirSync(result, {recursive: true});
    console.log(`Creating target dir: ${result}`);
    return result;
}
