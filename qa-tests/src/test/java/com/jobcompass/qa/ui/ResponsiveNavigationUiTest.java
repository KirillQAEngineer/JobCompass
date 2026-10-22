package com.jobcompass.qa.ui;

import com.jobcompass.qa.support.ApiSteps;
import com.jobcompass.qa.support.TestUser;
import com.jobcompass.qa.ui.pages.LoginPage;
import com.jobcompass.qa.ui.pages.MainPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("ui")
@Epic("JobCompass Web")
@Feature("Responsive navigation")
class ResponsiveNavigationUiTest extends UiTestBase {

    @Test
    void mobileAdminNavigationDoesNotOverflowViewport() {
        TestUser admin = ApiSteps.createAdmin("ui-admin");
        driver.manage().window().setSize(new Dimension(430, 900));
        new LoginPage(driver).open().login(admin);

        MainPage main = new MainPage(driver);
        main.waitUntilLoaded();
        main.waitForAnyText("Админ", "Admin");

        Number scrollWidth = (Number) ((JavascriptExecutor) driver)
                .executeScript("return document.documentElement.scrollWidth");
        Number viewportWidth = (Number) ((JavascriptExecutor) driver)
                .executeScript("return window.innerWidth");
        assertThat(scrollWidth.longValue()).isLessThanOrEqualTo(viewportWidth.longValue() + 1);
    }
}
