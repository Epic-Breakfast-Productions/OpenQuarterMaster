package tech.ebp.oqm.plugin.extItemSearch.service.searchServices.utils;

import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.ItemSearchService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.barcodeLookup.BarcodeLookupService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.dataKick.DatakickService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.rebrickable.RebrickableService;
import tech.ebp.oqm.plugin.extItemSearch.service.searchServices.providers.upcItemDb.UpcItemDbService;

import java.util.Collection;
import java.util.List;

public enum LookupService {
	BARCODE_LOOKUP(
		BarcodeLookupService.class,
		List.of(LookupSource.BARCODE_LOOKUP),
		List.of(LookupMethod.BARCODE, LookupMethod.TEXT)
	),
	DATAKICK(
		DatakickService.class,
		List.of(LookupSource.DATAKICK),
		List.of(LookupMethod.BARCODE)
	),
	REBRICKABLE(
		RebrickableService.class,
		List.of(LookupSource.REBRICKABLE),
		List.of(LookupMethod.PART_NUM, LookupMethod.SET_NUM, LookupMethod.TEXT)
	),
	UPC_ITEM_DB(
		UpcItemDbService.class,
		List.of(LookupSource.UPC_ITEM_DB),
		List.of(LookupMethod.BARCODE)
	),
	;
	
	public final Class<? extends ItemSearchService> searchClass;
	public final Collection<LookupSource> supportedSources;
	public final Collection<LookupMethod> supportedMethods;
	
	LookupService(
		Class<? extends ItemSearchService> searchClass,
		Collection<LookupSource> supportedSources,
		Collection<LookupMethod> supportedMethods
	) {
		this.searchClass = searchClass;
		this.supportedSources = supportedSources;
		 this.supportedMethods = supportedMethods;
	}
}
