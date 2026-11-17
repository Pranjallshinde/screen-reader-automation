package com.browserstack.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/** Thin wrapper around the BrowserStack screen reader executor commands (NVDA / VoiceOver). */
public class ScreenReader {

    public static final String NEXT = "next_focusable_item";
    public static final String PREVIOUS = "previous_focusable_item";
    public static final String ACTIVATE = "activate";

    public static class NavResult {
        public final boolean found;
        public final List<String> trail;

        NavResult(boolean found, List<String> trail) {
            this.found = found;
            this.trail = trail;
        }
    }

    private final JavascriptExecutor js;

    public ScreenReader(WebDriver driver) {
        this.js = (JavascriptExecutor) driver;
    }

    public void enable() {
        js.executeScript("browserstack_executor: {\"action\": \"screenReader\", \"arguments\": {\"enable\": \"true\"}}");
    }

    public void disable() {
        js.executeScript("browserstack_executor: {\"action\": \"screenReader\", \"arguments\": {\"enable\": \"false\"}}");
    }

    /** Presses a screen reader shortcut and returns the text NVDA spoke for it. */
    public String perform(String shortcut) {
        Object out = js.executeScript(
                "browserstack_executor: {\"action\": \"screenReaderAction\", \"arguments\": {\"command\": \""
                        + shortcut + "\"}}");
        return out == null ? "" : out.toString();
    }

    public String spoken() {
        Object out = js.executeScript("browserstack_executor: {\"action\": \"screenReaderSpokenDescription\"}");
        return out == null ? "" : out.toString();
    }

    /** Presses "next" until the spoken output matches, or maxSteps is reached. */
    public NavResult navigateUntil(Predicate<String> matcher, int maxSteps) {
        List<String> trail = new ArrayList<>();
        for (int i = 0; i < maxSteps; i++) {
            String text = perform(NEXT);
            trail.add(text);
            if (matcher.test(text)) {
                return new NavResult(true, trail);
            }
        }
        return new NavResult(false, trail);
    }
}
