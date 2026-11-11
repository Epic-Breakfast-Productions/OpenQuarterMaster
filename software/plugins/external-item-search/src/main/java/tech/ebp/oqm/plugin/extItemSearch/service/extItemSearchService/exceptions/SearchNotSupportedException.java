package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.exceptions;

import tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.utils.LookupMethod;

public class SearchNotSupportedException extends Exception {

	public SearchNotSupportedException(LookupMethod type) {
		super("Not supported: " + type);
	}
}
