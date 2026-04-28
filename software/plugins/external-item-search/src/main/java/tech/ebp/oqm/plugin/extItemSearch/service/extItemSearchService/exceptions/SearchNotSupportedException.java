package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.exceptions;

import tech.ebp.oqm.plugin.extItemSearch.model.SearchType;

public class SearchNotSupportedException extends Exception {
	
	public SearchNotSupportedException(SearchType type) {
		super("Not supported: " + type);
	}
}
