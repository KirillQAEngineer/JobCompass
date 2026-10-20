package com.jobcompass.qa.ui;

import com.jobcompass.qa.config.TestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

abstract class UiTestBase {
    protected WebDriver driver;

    @RegisterExtension
    final UiFailureExtension failureExtension = new UiFailureExtension(() -> driver);

    @BeforeEach
    void startBrowser() {
        ChromeOptions options = new ChromeOptions();
        if (TestConfig.headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--no-sandbox",
                "--disable-dev-shm-usage",
                "--disable-gpu",
                "--disable-search-engine-choice-screen",
                "--lang=ru-RU"
        );
        driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1440, 1000));
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
        }
    }
}
