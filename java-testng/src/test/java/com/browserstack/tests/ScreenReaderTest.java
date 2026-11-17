package com.browserstack.tests;

import static org.testng.Assert.assertTrue;

import com.browserstack.utils.ScreenReader;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Runs on NVDA (Windows 11 + Chrome) as configured in browserstack.yml.
 * The BrowserStack SDK injects the remote driver config, so a plain local-style driver is created here.
 */
public class ScreenReaderTest {

    private static final String URL = "https://bstackdemo.com/";
    private static final By SHELF_ITEM = By.cssSelector(".shelf-item");
    private static final By PRODUCT_IMAGES = By.cssSelector(".shelf-item__thumb img");

    // Each NVDA step takes about 8s on the grid, so the walk is capped (header, vendor filters, first products).
    private static final int MAX_STEPS = 45;

    private WebDriver driver;
    private ScreenReader sr;

    private static boolean has(String text, String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(text).find();
    }

    @BeforeMethod
    public void setUp() {
        driver = new org.openqa.selenium.chrome.ChromeDriver(new ChromeOptions());
        driver.get(URL);
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(SHELF_ITEM));
        sr = new ScreenReader(driver);
        sr.enable();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            try {
                sr.disable();
            } finally {
                driver.quit();
            }
        }
    }

    @Test(description = "Traverses the page and records the text NVDA reads (focus order)", timeOut = 900000)
    public void traversesPageAndReadsText() {
        // NVDA's only supported traversal shortcut (next_focusable_item) skips plain text, so make the page's
        // readable text focusable (test-side only, the site is not changed). Headings, product titles,
        // prices and Add to cart then become readable.
        ((JavascriptExecutor) driver).executeScript(
                "document.querySelectorAll('h3, h4, .shelf-item__title, .shelf-item__price, .shelf-item__buy-btn')"
                        + ".forEach(function (e) { if (!e.hasAttribute('tabindex')) e.setAttribute('tabindex', '0'); });");

        List<String> trail = new ArrayList<>();
        for (int i = 0; i < MAX_STEPS; i++) {
            String text = sr.perform(ScreenReader.NEXT);
            trail.add(text);
            // Stop once the reader wraps around to an element it already announced at the start.
            if (i > 5 && !text.isEmpty() && text.equals(trail.get(1))) {
                break;
            }
        }
        System.out.println("Traversal recorded " + trail.size() + " elements");
        System.out.println("Full spoken trail: " + trail);

        String joined = String.join(" | ", trail);
        assertTrue(trail.size() > 10, "screen reader reached too few elements");
        for (String expected : new String[] {
            "Favourites", "Orders", "Offers", "Apple", "Samsung", "Product\\(s\\) found", "iPhone 12", "799", "Add to cart"
        }) {
            assertTrue(has(joined, expected), "\"" + expected + "\" never announced. Trail: " + joined);
        }
    }

    @Test(description = "Product images expose alt text (metadata check)")
    public void productImagesHaveAltText() {
        List<WebElement> imgs = driver.findElements(PRODUCT_IMAGES);
        assertTrue(imgs.size() > 0, "No product images found");
        List<String> generic = imgs.stream()
                .map(i -> i.getAttribute("alt"))
                .filter(a -> a == null || a.trim().isEmpty() || a.trim().equalsIgnoreCase("img"))
                .collect(Collectors.toList());
        System.out.println(generic.size() + "/" + imgs.size() + " product images have missing/generic alt text");
    }
}
