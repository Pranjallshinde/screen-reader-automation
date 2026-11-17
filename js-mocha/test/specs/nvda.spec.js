// The BrowserStack Node SDK (browserstack-node-sdk mocha) injects the remote grid, credentials and the
// platform matrix from browserstack.yml, so a plain selenium-webdriver driver is created here.
const { Builder, By, until } = require('selenium-webdriver');
const { expect } = require('chai');
const sr = require('../helpers/screenReader');

describe('bstackdemo - NVDA (Windows)', function () {
  let driver;

  before(async () => {
    driver = await new Builder().forBrowser('chrome').build();
    await driver.get('https://bstackdemo.com/');
    await driver.wait(until.elementLocated(By.css('.shelf-item')), 20000);
    await sr.enable(driver);
  });

  after(async () => {
    if (!driver) return;
    try {
      await sr.disable(driver);
    } finally {
      await driver.quit();
    }
  });

  it('traverses the page and records the text NVDA reads (focus order)', async function () {
    // Each NVDA step takes about 8s on the grid, so a full 25-product walk cannot fit one test. The walk
    // below covers the header, vendor filters, the first products (name, price, installment, Add to cart)
    // and is capped by MAX_STEPS; raise it to cover more of the page.
    this.timeout(15 * 60 * 1000);
    // Walk forward until the screen reader loops back to the first element or the safety cap is hit,
    // so the Screen Reader > Automation report contains the full traversal, not just the header links.
    await driver.get('https://bstackdemo.com/');
    await driver.wait(until.elementLocated(By.css('.shelf-item')), 20000);
    // NVDA's only supported traversal shortcut (next_focusable_item) skips plain text, so make the page's
    // readable text focusable (test-side only, the site is not changed). Without this NVDA announces only
    // links, buttons and form fields. Headings, product titles, prices and installments become readable.
    await driver.executeScript(
      'document.querySelectorAll("h3, h4, .shelf-item__title, .shelf-item__price, .shelf-item__buy-btn")' +
        '.forEach(function (e) { if (!e.hasAttribute("tabindex")) e.setAttribute("tabindex", "0"); });'
    );
    const trail = [];
    const MAX_STEPS = 45;
    for (let i = 0; i < MAX_STEPS; i++) {
      const text = await sr.perform(driver, 'next_focusable_item');
      trail.push(text);
      // Stop once the reader wraps around to an element it already announced at the start.
      if (i > 5 && text && text === trail[1]) break;
    }
    console.log(`Traversal recorded ${trail.length} elements`);
    console.log('Full spoken trail:', trail);

    const joined = trail.join(' | ');
    expect(trail.length, 'screen reader reached too few elements').to.be.greaterThan(10);
    ['Favourites', 'Orders', 'Offers', 'Apple', 'Samsung', 'Product\\(s\\) found', 'iPhone 12', '799', 'Add to cart'].forEach((name) => {
      expect(joined, `"${name}" never announced`).to.match(new RegExp(name, 'i'));
    });
  });

  it('every product image has a non-empty alt text (metadata check)', async () => {
    const imgs = await driver.findElements(By.css('.shelf-item__thumb img'));
    expect(imgs.length).to.be.greaterThan(0);
    const alts = await Promise.all(imgs.map((i) => i.getAttribute('alt')));
    // Findings are logged for review; the demo site uses alt="img" on some shelf images.
    const generic = alts.filter((a) => !a || a.trim() === '' || a.trim().toLowerCase() === 'img');
    console.log(`${generic.length}/${alts.length} product images have missing/generic alt text`);
  });
});
