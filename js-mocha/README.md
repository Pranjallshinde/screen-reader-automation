# Screen reader automation: Mocha + BrowserStack (NVDA)

Screen reader automation for https://bstackdemo.com using Mocha and selenium-webdriver, run through the
BrowserStack Node SDK so results appear in the Accessibility Testing dashboard (Screen Reader > Automation).

- Screen reader: NVDA on Windows 11 + Chrome (the grid currently rejects screen reader commands on macOS/VoiceOver).
- The tests use the SDK's driver methods (`enableScreenReader`, `performScreenReaderAction`, `getSpokenDescription`,
  `disableScreenReader`). Raw `browserstack_executor` payloads bypass the SDK and are not reported.

## Setup
```bash
export BROWSERSTACK_USERNAME=<your username>
export BROWSERSTACK_ACCESS_KEY=<your access key>
npm install
npm test        # browserstack-node-sdk mocha
```

## Tests (`test/specs/nvda.spec.js`)
- **Page traversal:** tabs through the page with NVDA (`next_focusable_item`, the only supported traversal shortcut) and
  records what is read. The page's readable text (headings, product titles, prices, Add to cart) is made focusable
  in the test browser, because NVDA otherwise skips plain text. Capped at 45 steps (about 8s per step on the grid).
- **Alt text:** logs how many product images have missing or generic alt text.

## Notes
- `projectName` and `buildName` in `browserstack.yml` must stay fixed across runs (Accessibility "Automated tests").
- Credentials come only from environment variables; nothing secret is committed.
