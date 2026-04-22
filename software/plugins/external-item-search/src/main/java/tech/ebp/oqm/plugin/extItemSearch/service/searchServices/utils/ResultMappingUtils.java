package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils;

import com.fasterxml.jackson.databind.JsonNode;

public final class ResultMappingUtils {
	
	public static boolean isFieldEmpty(JsonNode curFieldVal) {
		if (curFieldVal == null || curFieldVal.isNull()) {
			return true;
		}
		if (curFieldVal.isTextual() && curFieldVal.asText().isBlank()) {
			return true;
		}
		if (curFieldVal.isArray() && curFieldVal.isEmpty()) {
			return true;
		}
		if (curFieldVal.isObject() && curFieldVal.isEmpty()) {
			return true;
		}
		return false;
	}
}
