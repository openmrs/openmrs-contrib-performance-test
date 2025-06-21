package org.openmrs.performance.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.http.HttpRequestActionBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.http.HttpDsl.http;
import static org.openmrs.performance.Constants.ADMIN_SUPER_USER_UUID;
import static org.openmrs.performance.Constants.ALKALINE_PHOSPHATE_CONCEPT;
import static org.openmrs.performance.Constants.CARE_SETTING_UUID;
import static org.openmrs.performance.Constants.ORDER;
import static org.openmrs.performance.Constants.ORDERABLE_LAB_TESTS;
import static org.openmrs.performance.Constants.OUTPATIENT_CLINIC_LOCATION_UUID;
import static org.openmrs.performance.Constants.TEST_ORDER_TYPE;
import static org.openmrs.performance.utils.CommonUtils.getAdjustedDateTimeAsString;
import static org.openmrs.performance.utils.CommonUtils.getCurrentDateTimeAsString;

public class LabTechHttpService extends HttpService {

	public HttpRequestActionBuilder getOrderTypeDetails(String orderType) {
		return http("Get the details of a orderType").get("/openmrs/ws/rest/v1/ordertype/" + orderType);
	}

	public HttpRequestActionBuilder getAllLabOrderDetails() {
		String customRepresentation = "custom:(display,names:(display),uuid,setMembers:(display,uuid,names:(display),"
		        + "setMembers:(display,uuid,names:(display))))";

		return http("Get all Lab order details")
		        .get("/openmrs/ws/rest/v1/concept/" + ORDERABLE_LAB_TESTS + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAllActiveLabOrders() {
		String customRepresentation = "custom:(uuid,orderNumber,patient:(uuid,display,person:(uuid,display,age,gender)),"
		        + "concept:(uuid,display),action,careSetting:(uuid,display,description,careSettingType,display),previousOrder,"
		        + "dateActivated,scheduledDate,dateStopped,autoExpireDate,encounter:(uuid,display),orderer:(uuid,display),"
		        + "orderReason,orderReasonNonCoded,orderType:(uuid,display,name,description,conceptClasses,parent),urgency,"
		        + "instructions,commentToFulfiller,display,fulfillerStatus,fulfillerComment,specimenSource,laterality,"
		        + "clinicalHistory,frequency,numberOfRepeats)";

		String startDate = URLEncoder.encode(getCurrentDateTimeAsString(), StandardCharsets.UTF_8);
		String endDate = URLEncoder.encode(getAdjustedDateTimeAsString(1), StandardCharsets.UTF_8);

		return http("Get all active Lab orders").get("/openmrs/ws/rest/v1/order?orderTypes=" + TEST_ORDER_TYPE + "&v="
		        + customRepresentation + "&excludeCanceledAndExpired=true&excludeDiscontinueOrders=true"
		        + "&activatedOnOrAfterDate=" + startDate + "&activatedOnOrBeforeDate=" + endDate);
	}

	public HttpRequestActionBuilder getLabOrdersByFullFillerStatus(String fullFillerStatus) {
		String customRepresentation = "custom:(uuid,orderNumber,patient:(uuid,display,person:(uuid,display,age,gender)),"
		        + "concept:(uuid,display),action,careSetting:(uuid,display,description,careSettingType,display),previousOrder,"
		        + "dateActivated,scheduledDate,dateStopped,autoExpireDate,encounter:(uuid,display),orderer:(uuid,display),"
		        + "orderReason,orderReasonNonCoded,orderType:(uuid,display,name,description,conceptClasses,parent),urgency,"
		        + "instructions,commentToFulfiller,display,fulfillerStatus,fulfillerComment,specimenSource,laterality,"
		        + "clinicalHistory,frequency,numberOfRepeats)";

		String startDate = URLEncoder.encode(getCurrentDateTimeAsString(), StandardCharsets.UTF_8);
		String endDate = URLEncoder.encode(getAdjustedDateTimeAsString(1), StandardCharsets.UTF_8);

		return http("Get Lab Orders by full filler status").get(
		    "/openmrs/ws/rest/v1/order?orderTypes=" + TEST_ORDER_TYPE + "&v=" + customRepresentation + "&fulfillerStatus="
		            + fullFillerStatus + "&activatedOnOrAfterDate=" + startDate + "&activatedOnOrBeforeDate=" + endDate);
	}

	public HttpRequestActionBuilder updateFullFillerStatus(String labOrderUuid, String fullFillerStatus,
	        String fulfillerComment) {
		return http("Update full filler status").post("/openmrs/ws/rest/v1/order/" + labOrderUuid + "/fulfillerdetails/")
		        .body(StringBody(session -> {
			        Map<String, Object> status = new HashMap<>();
			        status.put("fulfillerStatus", fullFillerStatus);
			        status.put("fulfillerComment", fulfillerComment);

			        try {
				        return new ObjectMapper().writeValueAsString(status);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder getEncounterDetails(String encounterUuid) {
		String customRepresentation = "custom:(uuid,encounterDatetime,encounterType,location:(uuid,name),patient:(uuid,"
		        + "display),encounterProviders:(uuid,provider:(uuid,name)),obs:(uuid,obsDatetime,voided,groupMembers,"
		        + "formFieldNamespace,formFieldPath,order:(uuid,display),concept:(uuid,name:(uuid,name)),value:(uuid,display,"
		        + "name:(uuid,name),names:(uuid,conceptNameType,name))))";

		return http("Get specific encounter details")
		        .get("/openmrs/ws/rest/v1/encounter/" + encounterUuid + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder getAlkalineConceptDetails() {
		String customRepresentation = "custom:(uuid,display,name,datatype,set,answers,hiNormal,hiAbsolute,hiCritical,"
		        + "lowNormal,lowAbsolute,lowCritical,units,allowDecimal,setMembers:(uuid,display,answers,datatype,hiNormal"
		        + ",hiAbsolute,hiCritical,lowNormal,lowAbsolute,lowCritical,units,allowDecimal))";

		return http("Get Alkaline concept details")
		        .get("/openmrs/ws/rest/v1/concept/" + ALKALINE_PHOSPHATE_CONCEPT + "?v=" + customRepresentation);
	}

	public HttpRequestActionBuilder updateSpecificEncounter(String encounterUuid) {
		return http("Update the specific encounter").post("/openmrs/ws/rest/v1/encounter/" + encounterUuid)
		        .body(StringBody(session -> {
			        Map<String, Object> payload = new HashMap<>();

			        List<Map<String, Object>> obsList = new ArrayList<>();

			        Map<String, Object> obsItem = new HashMap<>();
			        Map<String, String> concept = new HashMap<>();

			        concept.put("uuid", ALKALINE_PHOSPHATE_CONCEPT);
			        obsItem.put("concept", concept);
			        obsItem.put("status", "FINAL");

			        Map<String, String> order = new HashMap<>();
			        order.put("uuid", session.getString("labOrderUuid"));
			        obsItem.put("order", order);
			        obsItem.put("value", 4);
			        obsList.add(obsItem);

			        payload.put("obs", obsList);

			        try {
				        return new ObjectMapper().writeValueAsString(payload);
			        }
			        catch (JsonProcessingException e) {
				        throw new RuntimeException(e);
			        }
		        }));
	}

	public HttpRequestActionBuilder addLabOrder() {
		return http("Add a lab order").post("/openmrs/ws/rest/v1/order/").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("patient", session.getString("patient_uuid"));
			payload.put("location", OUTPATIENT_CLINIC_LOCATION_UUID);
			payload.put("encounterType", ORDER);
			payload.put("encounterDatetime", getCurrentDateTimeAsString());
			payload.put("visit", session.getString("visitUuid"));

			List<Object> obsList = new ArrayList<>();
			payload.put("obs", obsList);

			List<Map<String, Object>> ordersList = new ArrayList<>();
			Map<String, Object> orderItem = new HashMap<>();
			orderItem.put("action", "NEW");
			orderItem.put("type", "testorder");
			orderItem.put("patient", session.getString("patient_uuid"));
			orderItem.put("careSetting", CARE_SETTING_UUID);
			orderItem.put("orderer", ADMIN_SUPER_USER_UUID);
			orderItem.put("encounter", null);
			orderItem.put("concept", ALKALINE_PHOSPHATE_CONCEPT);
			orderItem.put("accessionNumber", "34");
			orderItem.put("urgency", "ROUTINE");
			orderItem.put("scheduledDate", null);

			ordersList.add(orderItem);
			payload.put("orders", ordersList);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		})).check(jsonPath("$.orders[0].uuid").saveAs("labOrderUuid"), jsonPath("$.uuid").saveAs("labEncounterUuid"));
	}

	public HttpRequestActionBuilder updateLabOrderCompletion() {
		return http("Update the Lab Order Completion").post("/openmrs/ws/rest/v1/order/").body(StringBody(session -> {
			Map<String, Object> payload = new HashMap<>();

			payload.put("previousOrder", session.getString("labOrderUuid"));
			payload.put("type", "testorder");
			payload.put("action", "DISCONTINUE");
			payload.put("careSetting", CARE_SETTING_UUID);
			payload.put("encounter", session.getString("labEncounterUuid"));
			payload.put("patient", session.getString("patient_uuid"));
			payload.put("concept", ALKALINE_PHOSPHATE_CONCEPT);

			Map<String, Object> orderer = new HashMap<>();
			orderer.put("uuid", ADMIN_SUPER_USER_UUID);
			orderer.put("display", "admin - Super User");

			payload.put("orderer", orderer);

			try {
				return new ObjectMapper().writeValueAsString(payload);
			}
			catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}));
	}

}
