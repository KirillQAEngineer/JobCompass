package com.jobcompass.qa.ui;

import com.jobcompass.qa.support.ApiSteps;
import com.jobcompass.qa.support.TestUser;
import com.jobcompass.qa.ui.pages.LoginPage;
import com.jobcompass.qa.ui.pages.MainPage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("ui")
@Epic("JobCompass Web")
@Feature("Authenticated user journey")
class UserJourneyUiTest extends UiTestBase {

    @Test
    void userCanLoginOpenAnalyticsProfileAndLogout() {
        TestUser user = ApiSteps.createVerifiedUser("ui-user");
        ApiSteps.authenticated(user)
                .contentType(ContentType.JSON)
                .body(ApiSteps.profilePayload())
                .put("/profile/me")
                .then().statusCode(200);

        LoginPage login = new LoginPage(driver).open();
        login.login(user);

        MainPage main = new MainPage(driver);
        main.waitUntilLoaded();
        main.openAnalytics();
        main.waitForAnyText("Аналитика навсегда", "Analytics forever");
        main.openProfile();
        main.logoutFromSettings();

        LoginPage loggedOut = new LoginPage(driver);
        loggedOut.waitForAnyText("Войти", "Sign In");
    }
}
