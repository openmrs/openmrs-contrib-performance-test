package org.openmrs.performance.http.get;

import io.gatling.javaapi.http.HttpRequestActionBuilder;
import org.openmrs.performance.utils.CommonUtils;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.http.HttpDsl.http;

public class AppointmentHttpService {

	public static HttpRequestActionBuilder getAppointmentsForSpecificDate(String date) {
		return http("Get Appointments for Specific Date").get("/openmrs/ws/rest/v1/appointment/all?forDate=" + date);
	}

	public static HttpRequestActionBuilder getAppointments(String patientUuid) {
		String startDate = CommonUtils.getCurrentDateTimeAsString();
		String requestBody = String.format("{\"patientUuid\":\"%s\",\"startDate\":\"%s\"}", patientUuid, startDate);

		return http("Get Appointments of Patient").post("/openmrs/ws/rest/v1/appointments/search")
		        .body(StringBody(requestBody));
	}
}
