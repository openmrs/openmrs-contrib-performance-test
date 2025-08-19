package org.openmrs.performance.http;

import io.gatling.javaapi.http.Http;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.bodyString;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;

public class FormBuilderHttpService extends HttpService {

	public HttpRequestActionBuilder getFrontendConfig() {
		return http("Get Frontend Config").get("/openmrs/spa/config-core_demo.json");
	}

	public HttpRequestActionBuilder getSessionInfo() {
		return http("Get Session Info").get("/openmrs/ws/rest/v1/session");
	}

	public HttpRequestActionBuilder getModuleInfo() {
		return http("Get Module Info").get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version");
	}

	public HttpRequestActionBuilder getAllForms() {
		return http("Get All Forms").get("/openmrs/ws/rest/v1/form?v=custom:(uuid,name,encounterType:(uuid,name),"
		        + "version,published,retired,resources:(uuid,name,dataType,valueReference))");
	}
}
