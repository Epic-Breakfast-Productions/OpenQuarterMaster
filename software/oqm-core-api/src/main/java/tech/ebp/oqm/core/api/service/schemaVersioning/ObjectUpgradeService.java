package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import tech.ebp.oqm.core.api.exception.ClassUpgraderNotFoundException;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.ObjectUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.OqmDbUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.service.mongo.MongoDbAwareService;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.StorageBlockUpgrader;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

@ApplicationScoped
@Slf4j
public class ObjectUpgradeService {

	private Map<Class<?>, ObjectUpgrader<?>> upgraderMap;
	private OqmDatabaseService oqmDatabaseService;
	private List<MongoDbAwareService<?,?,?>> oqmDbServices;

	public <C extends Versionable> ObjectUpgrader<C> getInstanceForClass(@NonNull Class<C> clazz) throws ClassUpgraderNotFoundException {
		if (!this.upgraderMap.containsKey(clazz)) {
			throw new ClassUpgraderNotFoundException(clazz);
		}
		return (ObjectUpgrader<C>) this.upgraderMap.get(clazz);
	}

	private void clearUpgraderMap() {
		this.upgraderMap = null;
	}

	@Inject
	public ObjectUpgradeService(
		OqmDatabaseService oqmDatabaseService
	) {
		this.oqmDatabaseService = oqmDatabaseService;
		//TODO:: populate oqmDbServices

		this.upgraderMap = Map.of(
			StorageBlock.class, new StorageBlockUpgrader()
		);
		;
	}

	public boolean upgradeRan() {
		return this.upgraderMap == null;
	}

	private <T extends MainObject> CollectionUpgradeResult upgradeOqmCollection(ClientSession cs, MongoCollection<Document> documentCollection, MongoCollection<T> typedCollection, Class<T> objectClass) throws ClassUpgraderNotFoundException {
		ObjectUpgrader<T> objectVersionBumper = this.getInstanceForClass(objectClass);
		CollectionUpgradeResult.Builder outputBuilder = CollectionUpgradeResult.builder()
			.collectionName(documentCollection.getNamespace().getCollectionName());

		StopWatch sw = StopWatch.createStarted();
		long numUpdated = 0;

		try (MongoCursor<Document> it = documentCollection.find().cursor()) {
			while (it.hasNext()) {
				Document doc = it.next();
				ObjectUpgradeResult<T> result = objectVersionBumper.upgrade(doc);

				if (result.wasUpgraded()) {
					numUpdated++;
					typedCollection.findOneAndReplace(
						cs,
						eq("id", result.getUpgradedObject().getId()),
						result.getUpgradedObject()
					);
				}
			}
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

		sw.stop();
		outputBuilder.timeTaken(Duration.of(sw.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS))
			.numObjectsUpgraded(numUpdated);

		return outputBuilder.build();
	}

	private <T extends MainObject> CollectionUpgradeResult upgradeOqmCollection(ClientSession dbCs, OqmMongoDatabase oqmDb, MongoDbAwareService<T, ?, ?> service) throws ClassUpgraderNotFoundException {
		log.info("Updating schema of oqm database service {} in ", service.getClass());
		String oqmDbId = oqmDb.getId().toHexString();
		CollectionUpgradeResult result = this.upgradeOqmCollection(
			dbCs,
			service.getDocumentCollection(oqmDbId),
			service.getTypedCollection(oqmDbId),
			service.getClazz()
		);

		log.info("DONE Updating schema of oqm database service {} in ", service.getClass());
		return result;
	}


	private OqmDbUpgradeResult upgradeOqmDb(OqmMongoDatabase oqmDb) {
		log.info("Updating schema of oqm database: {}", oqmDb);
		OqmDbUpgradeResult.Builder outputBuilder = OqmDbUpgradeResult.builder();
		StopWatch dbUpgradeTime = StopWatch.createStarted();

		List<CompletableFuture<CollectionUpgradeResult>> futures = new ArrayList<>();
		ClientSession cs = null;

		try {
			for (MongoDbAwareService<?,?,?> curService : this.oqmDbServices) {
				if (cs == null) {
					cs = curService.getNewClientSession(true);
				}
				ClientSession finalCs = cs;
				futures.add(
					CompletableFuture.supplyAsync(() -> {
						return upgradeOqmCollection(finalCs, oqmDb, curService);
					})
				);
			}
			if(cs != null) {
				cs.commitTransaction();
			}
		} finally {
			if(cs != null){
				cs.close();
			}
		}

		outputBuilder.collectionUpgradeResults(
			futures.stream().map(CompletableFuture::join).toList()
		);
		dbUpgradeTime.stop();
		outputBuilder.timeTaken(Duration.of(dbUpgradeTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));

		log.info("Done updating oqm database: {}", oqmDb);

		return outputBuilder.build();
	}

	public Optional<TotalUpgradeResult> updateSchema() {
		if (this.upgradeRan()) {
			return Optional.empty();
		}
		log.info("Upgrading the schema held in the Database.");

		TotalUpgradeResult.Builder totalResultBuilder = TotalUpgradeResult.builder();
		StopWatch totalTime = StopWatch.createStarted();

		//TODO:: migrate top levels


		List<CompletableFuture<OqmDbUpgradeResult>> resultMap = new ArrayList<>();
		for (OqmMongoDatabase curDb : this.oqmDatabaseService.listIterator()) {
			resultMap.add(CompletableFuture.supplyAsync(() -> {
					return upgradeOqmDb(curDb);
				})
			);
		}
		totalResultBuilder.dbUpgradeResults(resultMap.stream().map((CompletableFuture<OqmDbUpgradeResult> future) -> {
				try {
					return future.get();
				} catch (Throwable e) {
					throw new UpgradeFailedException("Failed to upgrade data in database.", e);
				}
			})
			.toList());
		totalTime.stop();
		totalResultBuilder.timeTaken(Duration.of(totalTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));

		log.info("DONE upgrading the schema held in the Database.");

		return Optional.of(totalResultBuilder.build());
	}
}
