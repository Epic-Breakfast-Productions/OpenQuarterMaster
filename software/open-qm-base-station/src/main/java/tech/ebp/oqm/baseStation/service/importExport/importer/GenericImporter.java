package tech.ebp.oqm.baseStation.service.importExport.importer;

import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedService;
import tech.ebp.oqm.lib.core.object.MainObject;

public abstract class GenericImporter<T extends MainObject, S extends SearchObject<T>> extends ObjectImporter<T, S, MongoHistoriedService<T,
																																		S>> {
	
	
	protected GenericImporter(MongoHistoriedService<T, S> mongoService) {
		super(mongoService);
	}
	
	
	
}