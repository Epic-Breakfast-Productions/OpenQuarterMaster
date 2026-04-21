package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.interfaces;

import tech.ebp.oqm.plugin.extItemSearch.model.lookupResult.ExtItemLookupResults;

public interface UrlSearching {
	
	ExtItemLookupResults searchUrl(String url) throws Exception;
}
