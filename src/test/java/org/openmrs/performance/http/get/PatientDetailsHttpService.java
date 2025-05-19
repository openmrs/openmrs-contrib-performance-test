package org.openmrs.performance.http.get;

import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.util.Set;
import java.util.StringJoiner;

import static io.gatling.javaapi.http.HttpDsl.http;

public class PatientDetailsHttpService {

	public static HttpRequestActionBuilder getPatientSummaryData(String patientUuid) {
		return http("Get Patient Summary Data").get("/openmrs/ws/fhir2/R4/Patient/" + patientUuid + "?_summary=data");
	}

	public static HttpRequestActionBuilder getPatientObservations(String patientUuid, Set<String> observationTypes) {
		// Join the observationTypes array into a single string with "%2C" as the delimiter
		StringJoiner joiner = new StringJoiner("%2C");
		for (String code : observationTypes) {
			joiner.add(code);
		}
		String codesParam = joiner.toString();

		// Construct the URL with the dynamically joined observationTypes
		String url = String.format(
		    "/openmrs/ws/fhir2/R4/Observation?subject:Patient=%s&code=%s&_summary=data&_sort=-date&_count=100", patientUuid,
		    codesParam);

		return http("Get Patient Observations").get(url);
	}
}
