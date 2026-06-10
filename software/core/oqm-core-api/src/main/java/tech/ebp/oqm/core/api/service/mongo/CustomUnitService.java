package tech.ebp.oqm.core.api.service.mongo;

import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.exception.db.DbNotFoundException;
import tech.ebp.oqm.core.api.health.utils.HasReadinessCheck;
import tech.ebp.oqm.core.api.health.utils.HealthStatus;
import tech.ebp.oqm.core.api.model.collectionStats.CollectionStats;
import tech.ebp.oqm.core.api.model.rest.search.CustomUnitSearch;
import tech.ebp.oqm.core.api.model.rest.unit.custom.NewCustomUnitRequest;
import tech.ebp.oqm.core.api.model.units.CustomUnitEntry;
import tech.ebp.oqm.core.api.model.units.UnitUtils;

import javax.measure.Unit;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to handle custom units, available to all OQM databases.
 */
@Slf4j
@ApplicationScoped
public class CustomUnitService extends TopLevelMongoService<CustomUnitEntry, CustomUnitSearch, CollectionStats> implements HasReadinessCheck {

    CustomUnitService() {
        super(CustomUnitEntry.class);
    }

    @Getter
    private final HealthStatus readinessStatus = new HealthStatus("Custom Unit Service");

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
        } catch (Exception e) {
            readinessStatus.markDown("Error occurred while reading custom units.");
            log.error("Error occurred while reading in custom units from database.", e);
            throw e;
        }
        log.info("Done reading in custom units.");
        readinessStatus.markUp("Custom units read in and registered");
    }

    public long getNextOrderValue() {
        CustomUnitEntry entry = this.listIterator(null, Sorts.descending("order"), null).first();

        if (entry == null) {
            return 0;
        }
        return entry.getOrder() + 1L;
    }

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

    public ObjectId add(ClientSession cs, @Valid CustomUnitEntry entry) {
        log.info("Adding new custom unit.");

        UnitUtils.registerAllUnits(entry);

        ObjectId id = null;
        if (cs == null) {
            id = this.getTypedCollection().insertOne(entry).getInsertedId().asObjectId().getValue();
        } else {
            id = this.getTypedCollection().insertOne(cs, entry).getInsertedId().asObjectId().getValue();
        }
        entry.setId(id);

        log.info("New custom unit: {}", entry);
        return entry.getId();
    }

    public ObjectId add(ClientSession cs, @Valid NewCustomUnitRequest ncur) {
        log.info("Adding new custom unit.");
        CustomUnitEntry newUnit = ncur.toCustomUnitEntry(this.getNextOrderValue());

        return this.add(cs, newUnit);
    }

    public List<CustomUnitEntry> list() {
        return this.listIterator(null, Sorts.ascending("order"), null).into(new ArrayList<>());
    }

    public void removeAll() {
        this.getTypedCollection().deleteMany(new BsonDocument());
    }

    @Override
    public int getCurrentSchemaVersion() {
        return CustomUnitEntry.CUR_SCHEMA_VERSION;
    }
}
