package tech.ebp.oqm.core.baseStation.utils;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.Map;

public class Searches {
	public static final MultivaluedMap<String, String> PARENT_SEARCH = new MultivaluedHashMap<>(Map.of("isParent", "true"));
}
