package org.openmrs.performance.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {
	
	public static List<String> extractConceptIds(String response) {
		List<String> conceptIds = new ArrayList<>();
		JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
		JsonArray entries = jsonObject.getAsJsonArray("entry");
		
		for (int i = 0; i < entries.size(); i++) {
			JsonObject resource = entries.get(i).getAsJsonObject().getAsJsonObject("resource");
			JsonArray coding = resource.getAsJsonObject("code").getAsJsonArray("coding");
			conceptIds.add(coding.get(0).getAsJsonObject().get("code").getAsString());
		}
		
		return conceptIds;
	}

}
