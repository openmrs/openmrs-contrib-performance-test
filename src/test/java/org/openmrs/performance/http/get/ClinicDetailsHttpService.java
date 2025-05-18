package org.openmrs.performance.http.get;

import io.gatling.javaapi.http.HttpRequestActionBuilder;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;

public class ClinicDetailsHttpService {
    public static HttpRequestActionBuilder loginRequest() {
        return http("Login").get("/openmrs/ws/rest/v1/session").header("Authorization", "Basic YWRtaW46QWRtaW4xMjM=")
                .check(jsonPath("$.authenticated").is("true"))
                .check(jsonPath("$.currentProvider.uuid").saveAs("currentUserUuid"));
    }

    public static HttpRequestActionBuilder getLocations() {
        return http("Get locations").get("/openmrs/ws/fhir2/R4/Location?_summary=data&_count=50&_tag=Login+Location");
    }

    public static HttpRequestActionBuilder selectLocation() {
        return http("Select Location").get("/openmrs/ws/rest/v1/session")
                .body(StringBody("{\"sessionLocation\":\"" + OUTPATIENT_CLINIC_LOCATION_UUID + "\"}"));
    }

    public static HttpRequestActionBuilder getAddressTemplate() {
        return http("Get Address Template").get("/openmrs/ws/rest/v1/addresstemplate");
    }

    public static HttpRequestActionBuilder getRelationshipTypes() {
        return http("Get Relationship Types").get("/openmrs/ws/rest/v1/relationshiptype?v=default");
    }

    public static HttpRequestActionBuilder getModuleInformation() {
        return http("Get Module Information").get("/openmrs/ws/rest/v1/module?v=custom:(uuid,version)");
    }

    public static HttpRequestActionBuilder getPatientIdentifierTypes() {
        return http("Get Patient Identifier Types").get(
                "/openmrs/ws/rest/v1/patientidentifiertype?v=custom:(display,uuid,name,format,required,uniquenessBehavior)");
    }

    public static HttpRequestActionBuilder getPrimaryIdentifierTermMapping() {
        return http("Get Primary Identifier Term Mapping")
                .get("/openmrs/ws/rest/v1/metadatamapping/termmapping?v=full&code=emr.primaryIdentifierType");
    }

    public static HttpRequestActionBuilder getVisitsOfLocation(String locationUuid) {
        String customRepresentation = """
                custom:(uuid,patient:(uuid,identifiers:(identifier,uuid,identifierType:(name,uuid)),person:(age,display,gender,
                uuid,attributes:(value,attributeType:(uuid,display)))),visitType:(uuid,name,display),location:
                (uuid,name,display),startDatetime,stopDatetime)&includeInactive=false&totalCount=true&location=
                """;
        return http("Get Visits").get(
                "/openmrs/ws/rest/v1/visit?v=" + customRepresentation + locationUuid);
    }

    public static HttpRequestActionBuilder getIdentifierSources() {
        return http("Get Identifier Source").get("/openmrs/ws/rest/v1/idgen/identifiersource?v=default");
    }

    public static HttpRequestActionBuilder getAutoGenerationOptions() {
        return http("Get Auto Generation Options").get("/openmrs/ws/rest/v1/idgen/autogenerationoption?v=full");
    }

    public static HttpRequestActionBuilder getPersonAttributeType(String personAttributeTypeUuid) {
        return http("Get Person Attribute Type").get("/openmrs/ws/rest/v1/personattributetype/" + personAttributeTypeUuid);
    }

    public static HttpRequestActionBuilder getOrderedAddressHierarchyLevels() {
        return http("Get Ordered Address Hierarchy Levels")
                .get("/openmrs/module/addresshierarchy/ajax/getOrderedAddressHierarchyLevels.form");
    }

}
