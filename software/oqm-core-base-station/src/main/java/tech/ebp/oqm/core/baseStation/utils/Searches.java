package tech.ebp.oqm.core.baseStation.utils;

import tech.ebp.oqm.lib.core.api.quarkus.runtime.restClient.searchObjects.StorageBlockSearch;

public class Searches {
	public static final StorageBlockSearch PARENT_SEARCH = StorageBlockSearch.builder().isParent(true).build();
}
