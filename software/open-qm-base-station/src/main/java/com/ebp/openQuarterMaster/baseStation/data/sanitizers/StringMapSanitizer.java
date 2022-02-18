package com.ebp.openQuarterMaster.baseStation.data.sanitizers;

import java.util.Map;

public abstract class StringMapSanitizer extends Sanitizer<Map<String, String>> {
	
	@Override
	public Map<String, String> sanitize(Map<String, String> object) {
		//TODO
		return object;
	}
}
