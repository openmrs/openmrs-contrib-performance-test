package org.openmrs.performance.scenarios;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class AppointmentBookingScenario extends Simulation {

    private static final String BASE_URL = "https://dev3.openmrs.org/openmrs";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "Admin123";
    private static final String AUTHORIZATION = "Basic YWRtaW46QWRtaW4xMjM=";

    private static final HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    private static final ScenarioBuilder scn = scenario("AppointmentBookingScenario")
        .exec(http("User Login")
            .post("/ws/rest/v1/session")
            .header("Authorization", AUTHORIZATION)
            .body(StringBody("{\"username\": \"" + USERNAME + "\", \"password\": \"" + PASSWORD + "\"}"))
            .check(status().is(200))
            .check(jsonPath("$.sessionId").saveAs("sessionId"))
        )
        .exec(session -> {
            String sessionId = session.getString("sessionId");
            return session.set("authToken", "Bearer " + sessionId);
        })
        .exec(http("Go to Home Page")
            .get("/spa/home")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Appointments")
            .get("/ws/rest/v1/appointmentscheduling/appointment")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Appointment Types")
            .get("/ws/rest/v1/appointmentscheduling/appointmenttype")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Providers")
            .get("/ws/rest/v1/provider")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Locations")
            .get("/ws/rest/v1/location")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Session Data")
            .get("/ws/rest/v1/session")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Time Slots")
            .get("/ws/rest/v1/appointmentscheduling/timeslot")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Appointment Tags")
            .get("/ws/rest/v1/appointmentscheduling/appointmenttag")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Open the Form")
            .get("/ws/rest/v1/form/{formUuid}")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Search for a Patient")
            .get("/ws/rest/v1/patient?q={patientName}")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Select a Patient")
            .get("/ws/rest/v1/patient/{patientUuid}")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Fetch Patient Identifiers")
            .get("/ws/rest/v1/patient/{patientUuid}/identifier")
            .header("Authorization", "#{authToken}")
            .check(status().is(200))
        )
        .exec(http("Book an Appointment")
            .post("/ws/rest/v1/appointment")
            .header("Authorization", "#{authToken}")
            .body(StringBody("{\"patient\": \"{patientUuid}\", \"appointmentType\": \"{appointmentTypeUuid}\", \"location\": \"{locationUuid}\", \"startDatetime\": \"{startDatetime}\", \"endDatetime\": \"{endDatetime}\"}"))
            .check(status().is(201))
        );

    {
        setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
    }
}
