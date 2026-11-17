// Screen reader helpers. The BrowserStack Node SDK (browserstack-node-sdk mocha) adds these methods to the
// selenium-webdriver driver and records every spoken output for the Accessibility dashboard's
// Screen Reader > Automation report. Raw `browserstack_executor` calls bypass the SDK and are NOT reported.
//   driver.enableScreenReader()
//   driver.performScreenReaderAction('next_focusable_item' | 'previous_focusable_item' | 'activate')
//   driver.getSpokenDescription()
//   driver.disableScreenReader()

async function enable(driver) {
  await driver.enableScreenReader();
}

async function disable(driver) {
  await driver.disableScreenReader();
}

// shortcut: 'next_focusable_item' | 'previous_focusable_item' | 'activate'
async function perform(driver, shortcut) {
  const out = await driver.performScreenReaderAction(shortcut);
  return typeof out === 'string' ? out : '';
}

async function spoken(driver) {
  const out = await driver.getSpokenDescription();
  return typeof out === 'string' ? out : JSON.stringify(out);
}

// Move forward up to maxSteps, collecting spoken output, until `matcher` hits.
async function navigateUntil(driver, matcher, maxSteps = 40) {
  const trail = [];
  for (let i = 0; i < maxSteps; i++) {
    const heard = await perform(driver, 'next_focusable_item');
    const text = heard || (await spoken(driver));
    trail.push(text);
    if (matcher(text)) return { found: true, trail };
  }
  return { found: false, trail };
}

module.exports = { enable, disable, perform, spoken, navigateUntil };
