package com.jobcompass.qa.ui.pages;

import com.jobcompass.qa.config.TestConfig;
import com.jobcompass.qa.support.TestUser;
import org.openqa.selenium.WebDriver;

public final class LoginPage extends FlutterPage {
    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public LoginPage open() {
        driver.get(TestConfig.webBaseUrl());
        enableAccessibility();
        waitForAnyText("JobCompass");
        return this;
    }

    public void switchToRegistration() {
        clickAnyText("Создать новый аккаунт", "Create a new account");
        waitForAnyText("Создать аккаунт", "Create Account");
    }

    public void login(TestUser user) {
        fillInput(0, user.email());
        fillInput(1, user.password());
        clickAnyText("Войти", "Sign In");
    }
}
