package tech.ebp.oqm.lib.core.api.java.search;

import java.util.LinkedHashMap;

public class QueryParams extends LinkedHashMap<String, QParamVal<?>> {
	
	public QueryParams addParam(String name, QParamVal<?> value) {
		put(name, value);
		return this;
	}
	
	public QueryParams addParam(String name, String value) {
		return this.addParam(name, QParamVal.of(value));
	}
	
	public QueryParams addParam(String name, String ... value) {
		return this.addParam(name, QParamVal.of(value));
	}
	
}
