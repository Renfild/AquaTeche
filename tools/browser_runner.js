// tools/browser_runner.js
// Standalone Playwright browser automation & preview runner for AquaTech
const fs = require('fs');
const path = require('path');

// Locate installed playwright package
const playwrightPath = 'C:/Users/xieto/AppData/Local/ms-playwright-go/1.57.0/package';
let playwright;
try {
  playwright = require(playwrightPath);
} catch (e) {
  try {
    playwright = require('playwright');
  } catch (err) {
    console.error('Playwright not found:', err.message);
    process.exit(1);
  }
}

const args = process.argv.slice(2);
const targetUrl = args.find(a => a.startsWith('http')) || 'https://aquateche.store';
const isHeaded = args.includes('--headed') || args.includes('-h');
const isMobile = args.includes('--mobile') || args.includes('-m');
const screenshotArg = args.find(a => a.startsWith('--screenshot='));
const outPath = screenshotArg ? screenshotArg.split('=')[1] : null;

(async () => {
  console.log(`[BrowserRunner] Launching browser (headed=${isHeaded}, mobile=${isMobile})...`);
  const browser = await playwright.chromium.launch({
    headless: !isHeaded,
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const contextOptions = isMobile ? {
    viewport: { width: 390, height: 844 },
    deviceScaleFactor: 2,
    isMobile: true,
    hasTouch: true,
    userAgent: 'Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1'
  } : {
    viewport: { width: 1440, height: 900 }
  };

  const context = await browser.newContext(contextOptions);
  const page = await context.newPage();

  console.log(`[BrowserRunner] Navigating to: ${targetUrl}`);
  await page.goto(targetUrl, { waitUntil: 'networkidle', timeout: 30000 });

  if (outPath) {
    const fullOut = path.resolve(outPath);
    await page.screenshot({ path: fullOut, fullPage: false });
    console.log(`[BrowserRunner] Screenshot saved to: ${fullOut}`);
  }

  if (isHeaded) {
    console.log('[BrowserRunner] Headed session active. Window will stay open for 15s...');
    await page.waitForTimeout(15000);
  }

  await browser.close();
  console.log('[BrowserRunner] Finished successfully.');
})();
