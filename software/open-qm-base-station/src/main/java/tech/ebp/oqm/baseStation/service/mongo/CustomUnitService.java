package tech.ebp.oqm.baseStation.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.baseStation.model.units.CustomUnitEntry;
import tech.ebp.oqm.baseStation.model.units.UnitUtils;
import tech.ebp.oqm.baseStation.rest.search.CustomUnitSearch;
import tech.ebp.oqm.baseStation.service.mongo.exception.DbNotFoundException;

import javax.measure.Unit;
import java.util.List;

@Slf4j
@ApplicationScoped
public class CustomUnitService extends MongoHistoriedObjectService<CustomUnitEntry, CustomUnitSearch> {
	
	CustomUnitService() {//required for DI
		super(null, null, null, null, null, null, false, null);
	}
	
	@Inject
	CustomUnitService(
		//            Validator validator,
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		@ConfigProperty(name = "quarkus.mongodb.database")
		String database
	) {
		super(
			objectMapper,
			mongoClient,
			database,
			CustomUnitEntry.class,
			false
		);
	}
	
	@PostConstruct
	void readInUnits() {
		log.info("Reading existing custom units from database...");
		
		try (
			MongoCursor<CustomUnitEntry> it = this.listIterator(null, Sorts.ascending("order"), null)
												  .batchSize(1)
												  .iterator()
		) {
			while (it.hasNext()) {
				CustomUnitEntry curEntry = it.next();
				log.debug("Registering unit {}", curEntry);
				UnitUtils.registerAllUnits(curEntry);
			}
		}
		log.info("Done reading in custom units.");
	}
	
	@WithSpan
	@Override
	public void ensureObjectValid(boolean newObject, CustomUnitEntry newOrChangedObject, ClientSession clientSession) {
		super.ensureObjectValid(newObject, newOrChangedObject, clientSession);
		//TODO:: ensure name,symbol, tostring? not same as any in default set or held
	}
	
	@WithSpan
	public long getNextOrderValue() {
		CustomUnitEntry entry = this.listIterator(null, Sorts.descending("order"), null).first();
		
		if (entry == null) {
			return 0;
		}
		return entry.getOrder() + 1L;
	}
	
	@WithSpan
	public CustomUnitEntry getFromUnit(ClientSession clientSession, Unit unit) {
		List<CustomUnitEntry> matchList = this.list(
			clientSession,
			Filters.eq("unitCreator.symbol", unit.getSymbol()),
			null,
			null
		);
		
		if (matchList.size() == 0) {
			throw new DbNotFoundException("Could not find custom unit " + unit, CustomUnitEntry.class);
		}
		if (matchList.size() != 1) {
			throw new DbNotFoundException(
				"Could not find custom unit " + unit + " - Too many matched units (" + matchList.size() + ")",
				CustomUnitEntry.class
			);
		}
		
		return matchList.get(0);
	}
}
