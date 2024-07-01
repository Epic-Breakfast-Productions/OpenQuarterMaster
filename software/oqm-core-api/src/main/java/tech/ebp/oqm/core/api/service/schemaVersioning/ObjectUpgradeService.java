package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import tech.ebp.oqm.core.api.exception.ClassUpgraderNotFoundException;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
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

@ApplicationScoped
@Slf4j
public class ObjectUpgradeService {

	private Map<Class<?>, ObjectUpgrader<?>> upgraderMap;
	private OqmDatabaseService oqmDatabaseService;
	private List<MongoDbAwareService> oqmDbServices;

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

		this.upgraderMap = Map.of(
			StorageBlock.class, new StorageBlockUpgrader()
		);
		;
	}

	public boolean upgradeRan() {
		return this.upgraderMap == null;
	}

	private CollectionUpgradeResult upgradeOqmCollection(OqmMongoDatabase oqmDb, MongoDbAwareService service) throws ClassUpgraderNotFoundException {
		CollectionUpgradeResult.Builder outputBuilder = CollectionUpgradeResult.builder();
		StopWatch collectionUpgradeTime = StopWatch.createStarted();

		List<CompletableFuture<CollectionUpgradeResult>> resultMap = new ArrayList<>();

		MongoCollection<?> collection = service.getCollection(oqmDb.getName());
		ObjectUpgrader<?> objectVersionBumper = this.getInstanceForClass(service.getClazz());

		for (MongoCursor<?> it = collection.find().cursor(); it.hasNext(); ) {
			//TODO:: get as bson, not object. feed to upgrader
//			objectVersionBumper.upgrade()

		}



		collectionUpgradeTime.stop();
		outputBuilder.timeTaken(Duration.of(collectionUpgradeTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));

		return outputBuilder.build();
	}


	private OqmDbUpgradeResult upgradeOqmDb(OqmMongoDatabase oqmDb) {
		OqmDbUpgradeResult.Builder outputBuilder = OqmDbUpgradeResult.builder();
		StopWatch dbUpgradeTime = StopWatch.createStarted();

		List<CompletableFuture<CollectionUpgradeResult>> resultMap = new ArrayList<>();

		for (MongoDbAwareService curService : this.oqmDbServices) {
			resultMap.add(
				CompletableFuture.supplyAsync(() -> {
					return upgradeOqmCollection(oqmDb, curService);
				})
			);
		}

		dbUpgradeTime.stop();
		outputBuilder.timeTaken(Duration.of(dbUpgradeTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));

		return outputBuilder.build();
	}

	public Optional<TotalUpgradeResult> updateSchema() {
		if (this.upgradeRan()) {
			return Optional.empty();
		}

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

		return Optional.of(totalResultBuilder.build());
	}
}
