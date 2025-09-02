package tech.ebp.oqm.plugin.extItemSearch.service.extItemSearchService.interfaces;

import tech.ebp.oqm.plugin.extItemSearch.model.ExtItemLookupResults;

public interface TextSearching {
	
	ExtItemLookupResults searchText(String url) throws Exception;

}
