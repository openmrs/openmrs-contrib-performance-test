//package io.gatling.demo;
//
//import java.time.Duration;
//import java.util.*;
//
//import io.gatling.javaapi.core.*;
//import io.gatling.javaapi.http.*;
//import io.gatling.javaapi.jdbc.*;
//
//import static io.gatling.javaapi.core.CoreDsl.*;
//import static io.gatling.javaapi.http.HttpDsl.*;
//import static io.gatling.javaapi.jdbc.JdbcDsl.*;
//
//public class RecordedSimulation extends Simulation {
//
//  private HttpProtocolBuilder httpProtocol = http
//    .baseUrl("https://test3.openmrs.org")
//    .inferHtmlResources(AllowList(), DenyList(".*\\.js", ".*\\.css", ".*\\.gif", ".*\\.jpeg", ".*\\.jpg", ".*\\.ico", ".*\\.woff", ".*\\.woff2", ".*\\.(t|o)tf", ".*\\.png", ".*\\.svg", ".*detectportal\\.firefox\\.com.*"))
//    .acceptHeader("application/json")
//    .acceptEncodingHeader("gzip, deflate, br")
//    .acceptLanguageHeader("en-GB,en;q=0.9")
//    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.2.1 Safari/605.1.15");
//
//  private Map<CharSequence, String> headers_0 = Map.ofEntries(
//    Map.entry("Content-Type", "application/json"),
//    Map.entry("Disable-WWW-Authenticate", "true"),
//    Map.entry("Origin", "https://test3.openmrs.org"),
//    Map.entry("Sec-Fetch-Dest", "empty"),
//    Map.entry("Sec-Fetch-Mode", "cors"),
//    Map.entry("Sec-Fetch-Site", "same-origin")
//  );
//
//  private Map<CharSequence, String> headers_2 = Map.ofEntries(
//    Map.entry("Sec-Fetch-Dest", "empty"),
//    Map.entry("Sec-Fetch-Mode", "cors"),
//    Map.entry("Sec-Fetch-Site", "same-origin")
//  );
//
//  private Map<CharSequence, String> headers_7 = Map.ofEntries(
//    Map.entry("Disable-WWW-Authenticate", "true"),
//    Map.entry("Sec-Fetch-Dest", "empty"),
//    Map.entry("Sec-Fetch-Mode", "cors"),
//    Map.entry("Sec-Fetch-Site", "same-origin")
//  );
//
//
//  private ScenarioBuilder scn = scenario("RecordedSimulation")
//    .exec(
//      http("request_0")
//        .post("/openmrs/ws/rest/v1/idgen/identifiersource/8549f706-7e85-4c1d-9424-217d50a2988b/identifier")
//        .headers(headers_0)
//        .body(StringBody("{}"))
//        .resources(
//          http("request_1")
//            .post("/openmrs/ws/rest/v1/patient/")
//            .headers(headers_0)
//            .body(StringBody("{\"uuid\":\"ebb74456-5232-4e06-a21b-8d2823189517\",\"person\":{\"uuid\":\"ebb74456-5232-4e06-a21b-8d2823189517\",\"names\":[{\"preferred\":true,\"givenName\":\"FN\",\"middleName\":\"MN\",\"familyName\":\"FN\"}],\"gender\":\"M\",\"birthdate\":\"2024-5-1\",\"birthdateEstimated\":false,\"attributes\":[],\"addresses\":[{}],\"dead\":false},\"identifiers\":[{\"identifier\":\"100026G\",\"identifierType\":\"05a29f94-c0ed-11e2-94be-8c13b969e334\",\"location\":\"44c3efb0-2583-4c80-a79e-1f756a03c0a1\",\"preferred\":true}]}"))),
//          http("request_2")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_3")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_4")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_5")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_6")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_7")
//            .get("/openmrs/ws/rest/v1/visit?patient=ebb74456-5232-4e06-a21b-8d2823189517&v=custom:(uuid,encounters:(uuid,encounterDatetime,form:(uuid,name),location:ref,encounterType:ref,encounterProviders:(uuid,display,provider:(uuid,display))),patient:(uuid,uuid),visitType:(uuid,name,display),attributes:(uuid,display,attributeType:(name,datatypeClassname,uuid),value),location:(uuid,name,display),startDatetime,stopDatetime)&includeInactive=false")
//            .headers(headers_7),
//          http("request_8")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_9")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_10")
//            .get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=ebb74456-5232-4e06-a21b-8d2823189517&code=5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100")
//            .headers(headers_2),
//          http("request_11")
//            .get("/openmrs/ws/rest/v1/visit-queue-entry?patient=ebb74456-5232-4e06-a21b-8d2823189517")
//            .headers(headers_7),
//          http("request_12")
//            .get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=ebb74456-5232-4e06-a21b-8d2823189517&code=5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5087AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5088AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5092AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5242AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C165095AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100")
//            .headers(headers_2),
//          http("request_13")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_14")
//            .get("/openmrs/ws/fhir2/R4/Observation?subject:Patient=ebb74456-5232-4e06-a21b-8d2823189517&code=5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA%2C1343AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA&_summary=data&_sort=-date&_count=100")
//            .headers(headers_2),
//          http("request_15")
//            .get("/openmrs/ws/fhir2/R4/Condition?patient=ebb74456-5232-4e06-a21b-8d2823189517&_count=100&_summary=data")
//            .headers(headers_2),
//          http("request_16")
//            .get("/openmrs/ws/rest/v1/order?patient=ebb74456-5232-4e06-a21b-8d2823189517&careSetting=6f0c9a92-6f24-11e3-af88-005056821db0&status=ACTIVE&orderType=131168f4-15f5-102d-96e4-000c29c2a5d7&v=custom:(uuid,dosingType,orderNumber,accessionNumber,patient:ref,action,careSetting:ref,previousOrder:ref,dateActivated,scheduledDate,dateStopped,autoExpireDate,orderType:ref,encounter:ref,orderer:(uuid,display,person:(display)),orderReason,orderReasonNonCoded,orderType,urgency,instructions,commentToFulfiller,drug:(uuid,display,strength,dosageForm:(display,uuid),concept),dose,doseUnits:ref,frequency:ref,asNeeded,asNeededCondition,quantity,quantityUnits:ref,numRefills,dosingInstructions,duration,durationUnits:ref,route:ref,brandName,dispenseAsWritten)")
//            .headers(headers_7),
//          http("request_17")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_18")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_19")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2),
//          http("request_20")
//            .get("/openmrs/ws/fhir2/R4/Patient/ebb74456-5232-4e06-a21b-8d2823189517?_summary=data")
//            .headers(headers_2)
//        )
//    );
//
//  {
//	  setUp(scn.injectOpen(atOnceUsers(1))).protocols(httpProtocol);
//  }
//}
