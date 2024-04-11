package tech.ebp.oqm.core.api.service.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
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
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitUtils;
import tech.ebp.oqm.core.api.rest.search.CustomUnitSearch;
import tech.ebp.oqm.core.api.service.mongo.exception.DbNotFoundException;
import tech.ebp.oqm.core.api.service.notification.HistoryEventNotificationService;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@Slf4j
@ApplicationScoped
public class CustomUnitService extends TopLevelMongoService<CustomUnitEntry> {
	
	CustomUnitService() {
		super(CustomUnitEntry.class);
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
	public long getNextOrderValue() {
		CustomUnitEntry entry = this.listIterator(null, Sorts.descending("order"), null).first();
		
		if (entry == null) {
			return 0;
		}
		return entry.getOrder() + 1L;
	}
	
	@WithSpan
	public CustomUnitEntry getFromUnit(ClientSession clientSession, Unit unit) {
		List<CustomUnitEntry> matchList = this.listIterator(
			clientSession,
			Filters.eq("unitCreator.symbol", unit.getSymbol()),
			null,
			null
		).into(new ArrayList<>());
		
		if (matchList.isEmpty()) {
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
	
	public CollectionStats collectionStats() {
		return CollectionStats.builder()
				   .size(this.getCollection().countDocuments())
				   .build();
	}
}
