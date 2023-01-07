package tech.ebp.oqm.baseStation.service.importExport.importer;

import com.mongodb.client.ClientSession;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.baseStation.service.mongo.CustomUnitService;
import tech.ebp.oqm.lib.core.object.interactingEntity.InteractingEntity;
import tech.ebp.oqm.lib.core.units.CustomUnitEntry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;

@ApplicationScoped
public class UnitImporter extends ObjectImporter<CustomUnitEntry, CustomUnitSearch, CustomUnitService>{
	
	@Inject
	public UnitImporter(CustomUnitService mongoService) {
		super(mongoService);
	}
	
	@Override
	public long readInObjects(ClientSession clientSession, Path directory, InteractingEntity importingEntity) throws IOException {
		//TODO
		return 0;
	}
	
	
}
