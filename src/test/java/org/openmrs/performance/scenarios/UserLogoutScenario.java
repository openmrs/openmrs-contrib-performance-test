package org.openmrs.performance.scenarios;

import io.gatling.javaapi.core.ScenarioBuilder;
import org.openmrs.performance.registries.Registry;
import org.openmrs.performance.http.HttpService;

import static io.gatling.javaapi.core.CoreDsl.*;

public class UserLogoutScenario extends Scenario<Registry<HttpService>> {

    public UserLogoutScenario(float scenarioLoadShare, Registry<HttpService> registry) {
        super(scenarioLoadShare, registry);
    }

    @Override
    public ScenarioBuilder getScenarioBuilder() {
        return scenario("User Logout Scenario")
            .exec(registry.httpService.logoutRequest())
            .pause(1);
    }
}
