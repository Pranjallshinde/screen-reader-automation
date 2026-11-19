# Screen reader automation on BrowserStack (bstackdemo.com)

Sample test suites that run **screen reader automation (NVDA on Windows 11 + Chrome)** against
<https://bstackdemo.com> on BrowserStack, plus a GitHub Actions workflow to trigger them on demand.

## Use case
Use this repo to:
- See how to drive a screen reader (enable it, move focus, read the spoken text) from automated tests.
- Capture what a screen reader user actually hears while moving through a page, and check key elements are announced
  (header links, vendor filters, product titles, prices, Add to cart) and that product images have alt text.
- Publish results to the BrowserStack **Accessibility** dashboard (Screen Reader > Automation).
- Run the same checks from CI with one click, on your own or a demo BrowserStack account.

## Repo layout
| Folder | Framework | Notes |
|---|---|---|
| [`js-mocha/`](js-mocha) | JavaScript + Mocha + selenium-webdriver | Run through `browserstack-node-sdk`; feeds the Screen Reader > Automation report. |
| [`java-testng/`](java-testng) | Java 11 + TestNG + Selenium | Uses the BrowserStack Java SDK. |

Each folder has its own README and a `browserstack.yml` (platform: Windows 11, Chrome latest, `srAutomation` on).

## Run locally
```bash
export BROWSERSTACK_USERNAME=<your username>
export BROWSERSTACK_ACCESS_KEY=<your access key>

# JavaScript + Mocha
cd js-mocha && npm install && npm test

# Java + TestNG
cd java-testng && mvn test
```

## GitHub Actions workflow
File: [`.github/workflows/browserstack-web.yml`](.github/workflows/browserstack-web.yml) (**BrowserStack Web Tests**).

### Run it manually
1. Go to **Actions > BrowserStack Web Tests > Run workflow**.
2. Pick the **framework** from the dropdown: `js-mocha` or `java-testng`.
3. Optionally enter a BrowserStack **username** and **access key** to run on that account (for example a demo
   account). Leave both empty to use the repository secrets.
4. Optionally set a **build name**. Leave empty to use the one in `browserstack.yml`.

### Other triggers
- Push or pull request to `main`: runs `js-mocha` with the repository secrets.
- Nightly at 02:30 UTC: runs `js-mocha` with the repository secrets.

### What a run does
1. Checks out the repo.
2. **Loads and masks credentials**: takes the run inputs if given, otherwise the repo secrets, masks both values with
   `::add-mask::` and only then exposes them to later steps.
3. Sets up Node 20 (`js-mocha`) or Java 11 (`java-testng`), installs dependencies.
4. Runs `npm test` or `mvn test`, which starts the BrowserStack session.
5. Uploads the `log/` folders as an artifact.

### One-time setup
Add repository secrets (Settings > Secrets and variables > Actions):
- `BROWSERSTACK_USERNAME`
- `BROWSERSTACK_ACCESS_KEY`

### Credentials and security
- Secrets and entered credentials are masked in the workflow logs.
- GitHub shows workflow **inputs in plain text on the run page** (masking cannot hide them there). Type credentials in
  only for demo or throwaway accounts; keep real keys in repository secrets and leave the fields empty.

## Viewing results
Open the BrowserStack dashboard:
- **Automate**: the build `bstackdemo-screen-reader-mocha` (project "Screen Reader Automation Demo").
- **Accessibility > Screen Reader > Automation**: the spoken-text trail for each test.

## Supported screen readers
NVDA on Windows 11 + Chrome is supported and verified for the JS suite. VoiceOver (macOS) is currently rejected by the
BrowserStack grid, and JAWS is not offered for screen reader automation.
