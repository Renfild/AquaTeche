const { chromium } = require('./tools_npm/node_modules/playwright');
const fs = require('fs');
const path = require('path');

const FILE_TXT = 'C:\\Users\\xieto\\Desktop\\AquaTech\\cf_token.txt';

(async () => {
  console.log('[Playwright] Launching Chrome browser...');
  let browser;
  try {
    browser = await chromium.launch({
      channel: 'chrome',
      headless: false,
      args: ['--start-maximized']
    });
  } catch (err) {
    browser = await chromium.launch({
      channel: 'msedge',
      headless: false,
      args: ['--start-maximized']
    });
  }

  const context = await browser.newContext({ viewport: null });
  const page = await context.newPage();

  console.log('[Playwright] Navigating to Cloudflare API token creation page...');
  await page.goto('https://dash.cloudflare.com/profile/api-tokens/create', { waitUntil: 'domcontentloaded' });
  await page.waitForTimeout(3000);

  try {
    const templateBtn = page.locator('tr:has-text("Edit Cloudflare Workers") button, div:has-text("Edit Cloudflare Workers") button').first();
    if (await templateBtn.isVisible({ timeout: 6000 })) {
      console.log('[Playwright] Step 1: Clicking "Use template"...');
      await templateBtn.click();
      await page.waitForTimeout(2000);
    }

    const summaryBtn = page.locator('button:has-text("Continue to summary")').first();
    if (await summaryBtn.isVisible({ timeout: 6000 })) {
      console.log('[Playwright] Step 2: Clicking "Continue to summary"...');
      await summaryBtn.click();
      await page.waitForTimeout(2000);
    }

    const createFinalBtn = page.locator('button:has-text("Create Token")').first();
    if (await createFinalBtn.isVisible({ timeout: 6000 })) {
      console.log('[Playwright] Step 3: Clicking final "Create Token"...');
      await createFinalBtn.click();
      await page.waitForTimeout(4000);
    }
  } catch (e) {
    console.log('[Playwright] Navigation note:', e.message);
  }

  console.log('[Playwright] Extracting 40-char token...');
  let extractedToken = null;
  for (let i = 0; i < 40; i++) {
    try {
      const candidates = await page.evaluate(() => {
        const els = Array.from(document.querySelectorAll('input, code, textarea, span'));
        return els
          .map((el) => (el.value || el.textContent || '').trim())
          .filter((s) => s.length >= 38 && s.length <= 45 && /^[A-Za-z0-9_\-]+$/.test(s) && !s.includes('http') && !s.includes('Cloudflare'));
      });

      if (candidates && candidates.length > 0) {
        extractedToken = candidates[0];
        break;
      }
    } catch (e) {}
    await page.waitForTimeout(1500);
  }

  if (extractedToken) {
    const cleanToken = String(extractedToken).trim();
    fs.writeFileSync(FILE_TXT, cleanToken, 'utf-8');
    fs.writeFileSync('C:\\Users\\xieto\\Desktop\\AquaTech\\.cf_token', cleanToken, 'utf-8');
    console.log('[Playwright] TOKEN_WRITTEN_TO_CF_TOKEN_TXT length:', cleanToken.length);
  } else {
    console.log('[Playwright] Token not found.');
  }

  await browser.close();
})();
