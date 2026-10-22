package com.jobcompass.qa.ui;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.util.function.Supplier;

final class UiFailureExtension implements AfterTestExecutionCallback {
    private final Supplier<WebDriver> driverSupplier;

    UiFailureExtension(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()) {
            return;
        }

        WebDriver driver = driverSupplier.get();
        if (driver == null) {
            return;
        }

        try {
            byte[] screenshot = ((TakesScreenshot) driver)
                    .getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment(
                    "Failure screenshot",
                    "image/png",
                    new ByteArrayInputStream(screenshot),
                    ".png"
            );
            Allure.addAttachment("Page source", "text/html", driver.getPageSource());
        } catch (RuntimeException ignored) {
            // The browser may already be unavailable; the original failure is preserved.
        }
    }
}
