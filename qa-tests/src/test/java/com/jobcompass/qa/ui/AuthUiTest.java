package com.jobcompass.qa.ui;

import com.jobcompass.qa.ui.pages.LoginPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("JobCompass Web")
@Feature("Authentication UI")
class AuthUiTest extends UiTestBase {

    @Test
    void passwordRemainsEditableAfterMinimumLengthValidation() {
        driver.manage().window().setSize(new Dimension(430, 900));
        LoginPage page = new LoginPage(driver).open();
        page.switchToRegistration();

        page.fillInput(0, "Java QA User");
        page.fillInput(1, "java.qa@example.com");
        page.fillInput(2, "short");
        page.fillInput(3, "short");
        page.clickAnyText("Создать аккаунт", "Create Account");
        page.waitForAnyText(
                "Пароль должен быть не короче 9 символов",
                "Password must be at least 9 characters"
        );

        page.fillInput(2, "123456789");
        assertThat(page.inputValue(2)).isEqualTo("123456789");
    }

    @Test
    void mobileEmailFieldCanReceiveKeyboardFocus() {
        driver.manage().window().setSize(new Dimension(390, 844));
        LoginPage page = new LoginPage(driver).open();
        page.fillInput(0, "mobile@example.com");

        assertThat(page.inputValue(0)).isEqualTo("mobile@example.com");
    }
}
