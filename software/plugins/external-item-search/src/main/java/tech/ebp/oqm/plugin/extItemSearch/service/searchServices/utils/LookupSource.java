package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils;

import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.barcodeLookup.BarcodeLookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.dataKick.DatakickService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable.RebrickableService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.upcItemDb.UpcItemDbService;

public enum LookupSource {
	BARCODE_LOOKUP,
	DATAKICK,
	REBRICKABLE,
	UPC_ITEM_DB,
	;
	
}
