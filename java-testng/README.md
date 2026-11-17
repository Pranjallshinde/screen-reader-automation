# Screen reader automation: Java TestNG + BrowserStack (NVDA)

Screen reader automation for https://bstackdemo.com using Java 11, TestNG and Selenium, run through the
BrowserStack Java SDK (`browserstack.yml` + javaagent).

- Screen reader: NVDA on Windows 11 + Chrome (the grid currently rejects screen reader commands on macOS/VoiceOver).
- Commands are sent as `browserstack_executor` payloads (see `ScreenReader.java`):
  - enable / disable: `{"action":"screenReader","arguments":{"enable":"true"|"false"}}`
  - shortcut (returns the spoken text): `{"action":"screenReaderAction","arguments":{"command":"next_focusable_item"}}`
  - last spoken output: `{"action":"screenReaderSpokenDescription"}`
- `srAutomation: true` and `accessibility: true` are set in `browserstack.yml`. BrowserStack documents the Screen Reader
  scan report for the Node.js and C# SDKs, so for the Screen Reader > Automation report use the JavaScript Mocha project
  (`js-mocha/`) in this repo; this project shows in Automate and Accessibility "Automated tests".

## Setup
```bash
export BROWSERSTACK_USERNAME=<your username>
export BROWSERSTACK_ACCESS_KEY=<your access key>
mvn test
```

## Tests (`ScreenReaderTest`)
- **Page traversal:** tabs through the page with NVDA (`next_focusable_item`, the only supported traversal shortcut) and
  records what is read. The page's readable text (headings, product titles, prices, Add to cart) is made focusable in the
  test browser, because NVDA otherwise skips plain text. Capped at 45 steps (about 8s per step on the grid).
- **Alt text:** logs how many product images have missing or generic alt text.

## Notes
- Credentials come only from environment variables; nothing secret is committed.
