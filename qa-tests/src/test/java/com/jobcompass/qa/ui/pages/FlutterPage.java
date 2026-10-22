package com.jobcompass.qa.ui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class FlutterPage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    public FlutterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(30));
    }

    public void enableAccessibility() {
        wait.until(webDriver -> "complete".equals(
                ((JavascriptExecutor) webDriver).executeScript("return document.readyState")
        ));
        wait.until(webDriver -> !webDriver.findElements(
                By.cssSelector("flt-semantics[aria-label], flt-semantics input")
        ).isEmpty());
    }

    public void clickAnyText(String... alternatives) {
        clickText(false, alternatives);
    }

    public void clickLastText(String... alternatives) {
        clickText(true, alternatives);
    }

    protected void clickNavigationDestination(int index) {
        wait.until(webDriver -> {
            List<WebElement> destinations = webDriver.findElements(By.cssSelector(
                    "flt-semantics[role='button'][flt-tappable]"
            ));
            if (destinations.size() <= index) {
                return false;
            }
            try {
                ((JavascriptExecutor) webDriver).executeScript(
                        "arguments[0].click();",
                        destinations.get(index)
                );
                return true;
            } catch (StaleElementReferenceException ignored) {
                return false;
            }
        });
    }

    public void waitForAnyText(String... alternatives) {
        wait.until(webDriver -> Arrays.stream(alternatives)
                .anyMatch(value -> webDriver.getPageSource().contains(value)));
    }

    public List<WebElement> visibleInputs() {
        return driver.findElements(By.cssSelector("input, textarea")).stream()
                .filter(WebElement::isDisplayed)
                .toList();
    }

    public void fillInput(int index, String value) {
        WebElement input = wait.until(webDriver -> {
            List<WebElement> inputs = visibleInputs();
            return inputs.size() > index ? inputs.get(index) : null;
        });
        input.click();
        String currentValue = input.getAttribute("value");
        input.sendKeys(Keys.END);
        for (int character = 0; character < currentValue.length(); character++) {
            input.sendKeys(Keys.BACK_SPACE);
        }
        input.sendKeys(value);
    }

    public String inputValue(int index) {
        return visibleInputs().get(index).getAttribute("value");
    }

    public void waitUntil(java.util.function.Function<WebDriver, Boolean> condition) {
        wait.until((ExpectedCondition<Boolean>) condition::apply);
    }

    private void clickText(boolean useLastMatch, String... alternatives) {
        wait.until(webDriver -> Boolean.TRUE.equals(
                ((JavascriptExecutor) webDriver).executeScript("""
                        const expectedValues = arguments[0];
                        const useLastMatch = arguments[1];
                        const normalize = (value) => (value || '').replace(/\\s+/g, ' ').trim();
                        for (const expected of expectedValues) {
                          const matches = [...document.querySelectorAll(
                            'flt-semantics, [aria-label], button, span'
                          )].filter((element) => {
                            const label = normalize(element.getAttribute('aria-label'));
                            const content = normalize(element.textContent);
                            return label === expected || label.includes(expected) || content === expected;
                          });
                          const interactive = matches.filter((element) =>
                            element.hasAttribute('flt-tappable') ||
                            element.getAttribute('role') === 'button'
                          );
                          const candidates = interactive.length > 0 ? interactive : matches;
                          if (candidates.length > 0) {
                            const target = useLastMatch
                              ? candidates[candidates.length - 1]
                              : candidates[0];
                            target.click();
                            return true;
                          }
                        }
                        return false;
                        """, List.of(alternatives), useLastMatch)
        ));
    }
}
