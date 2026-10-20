package com.jobcompass.qa.ui.pages;

import org.openqa.selenium.WebDriver;

public final class MainPage extends FlutterPage {
    public MainPage(WebDriver driver) {
        super(driver);
    }

    public void waitUntilLoaded() {
        waitForAnyText("Главная", "Home");
    }

    public void openAnalytics() {
        clickNavigationDestination(3);
    }

    public void openProfile() {
        clickNavigationDestination(4);
        waitForAnyText("Профиль", "Profile");
    }

    public void logoutFromSettings() {
        clickAnyText("Настройки", "Settings");
        waitForAnyText("Настройки", "Settings");
        clickAnyText("Выйти", "Logout");
        clickLastText("Выйти", "Logout");
    }
}
