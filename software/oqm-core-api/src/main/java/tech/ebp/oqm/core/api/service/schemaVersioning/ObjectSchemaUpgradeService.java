package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import tech.ebp.oqm.core.api.config.CoreApiInteractingEntity;
import tech.ebp.oqm.core.api.exception.ClassUpgraderNotFoundException;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.model.object.MainObject;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.history.details.FromSchemaUpgradeDetail;
import tech.ebp.oqm.core.api.model.object.history.events.SchemaUpgradeEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.InventoryItem;
import tech.ebp.oqm.core.api.model.object.storage.storageBlock.StorageBlock;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.ObjectUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.OqmDbUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeOverallCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.mongo.InventoryItemService;
import tech.ebp.oqm.core.api.service.mongo.MongoDbAwareService;
import tech.ebp.oqm.core.api.service.mongo.MongoHistoriedObjectService;
import tech.ebp.oqm.core.api.service.mongo.MongoObjectService;
import tech.ebp.oqm.core.api.service.mongo.MongoService;
import tech.ebp.oqm.core.api.service.mongo.StorageBlockService;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.InventoryItemSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.storageBlock.StorageBlockSchemaUpgrader;
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
public class ObjectSchemaUpgradeService {
	
	/** Map of upgraders to provide easy access to which upgraders for which object class. */
	private Map<Class<?>, ObjectSchemaUpgrader<?>> upgraderMap;
	/** The main oqm database service. */
	private OqmDatabaseService oqmDatabaseService;
	/** Map of classes to their corresponding oqm mongo db service, for easy access. */
	private Map<Class<?>, MongoDbAwareService<?, ?, ?>> oqmDbServices;
	/** Data structure for easy grouping of services that can/ should have their schema updated at the same time, and those groups in order. */
	private List<List<MongoDbAwareService<?, ?, ?>>> dbAwareUpgradeGroups;
	private TotalUpgradeResult startupUpgradeResult = null;
	
	@Getter(AccessLevel.PRIVATE)
	CoreApiInteractingEntity coreApiInteractingEntity;
	
	@ConfigProperty(name = "quarkus.uuid")
	String instanceUuid;
	
	@Inject
	public ObjectSchemaUpgradeService(
		CoreApiInteractingEntity coreApiInteractingEntity,
		@ConfigProperty(name = "quarkus.uuid")
		String instanceUuid,
		OqmDatabaseService oqmDatabaseService,
		StorageBlockService storageBlockService,
		InventoryItemService inventoryItemService
	) {
		this.coreApiInteractingEntity = coreApiInteractingEntity;
		this.instanceUuid = instanceUuid;
		this.oqmDatabaseService = oqmDatabaseService;
		
		//This insertion order here is the order of which these are each processed.
		//TODO:: populate rest of oqmDbServices
		this.oqmDbServices = new LinkedHashMap<>();
		this.oqmDbServices.put(storageBlockService.getClazz(), storageBlockService);
		this.oqmDbServices.put(inventoryItemService.getClazz(), inventoryItemService);
		
		this.dbAwareUpgradeGroups = List.of(
			List.of(
				storageBlockService
			),
			List.of(
				inventoryItemService
			)
		);
		
		this.upgraderMap = Map.of(
			StorageBlock.class, new StorageBlockSchemaUpgrader(),
			InventoryItem.class, new InventoryItemSchemaUpgrader()
		);
	}
	
	public Optional<TotalUpgradeResult> getStartupUpgradeResult() {
		return Optional.ofNullable(this.startupUpgradeResult);
	}
	
	public boolean upgradeRan() {
		return this.startupUpgradeResult == null;
	}
	
	
	public <C extends Versionable> ObjectSchemaUpgrader<C> getUpgrader(@NonNull Class<C> clazz) throws ClassUpgraderNotFoundException {
		if (!this.upgraderMap.containsKey(clazz)) {
			throw new ClassUpgraderNotFoundException(clazz);
		}
		return (ObjectSchemaUpgrader<C>) this.upgraderMap.get(clazz);
	}
	
	private void clearUpgraderMap() {
		this.upgraderMap = null;
	}
	
	/**
	 * Handles the actual upgrading of schema data in a collection. Iterates over all elements in collection, upgrading each (if necessary).
	 *
	 * @param cs
	 * @param documentCollection
	 * @param typedCollection
	 * @param objectClass
	 * @param <T>
	 *
	 * @return
	 * @throws ClassUpgraderNotFoundException
	 */
	private <T extends MainObject> CollectionUpgradeResult upgradeOqmCollection(
		String upgradeId,
		ClientSession cs,
		String oqmDbId,
		MongoCollection<Document> documentCollection,
		MongoCollection<T> typedCollection,
		Class<T> objectClass
	) throws ClassUpgraderNotFoundException {
		ObjectSchemaUpgrader<T> objectVersionBumper = this.getUpgrader(objectClass);
		CollectionUpgradeResult.Builder outputBuilder = CollectionUpgradeResult.builder()
															.collectionName(documentCollection.getNamespace().getCollectionName());
		UpgradeOverallCreatedObjectsResults createdObjectResults = new UpgradeOverallCreatedObjectsResults();
		outputBuilder.createdObjects(createdObjectResults);
		
		StopWatch sw = StopWatch.createStarted();
		long numUpdated = 0;
		
		if (objectVersionBumper.upgradesAvailable()) {
			//TODO:: add search for any objects with versions less than current.
			try (MongoCursor<Document> it = documentCollection.find().cursor()) {
				while (it.hasNext()) {
					Document doc = it.next();
					ObjectUpgradeResult<T> result = objectVersionBumper.upgrade(doc);
					
					if (result.wasUpgraded()) {
						numUpdated++;
						SchemaUpgradeEvent schemaUpgradeEvent = SchemaUpgradeEvent.builder()
																	.upgradeId(upgradeId)
																	.id(new ObjectId())
																	.fromVersion(result.getOldVersion())
																	.toVersion(result.getUpgradedObject().getSchemaVersion())
																	.build();
						if (result.hasUpgradedCreatedObjects()) {
							//persist created objects.
							FromSchemaUpgradeDetail detail = new FromSchemaUpgradeDetail(
								upgradeId,
								objectClass.getSimpleName(),
								result.getUpgradedObject().getId(),
								schemaUpgradeEvent.getObjectId()
							);
							result.getUpgradeCreatedObjects()
								.forEach((newObjClass, newObjects)->{
									MongoDbAwareService<?, ?, ?> service = this.oqmDbServices.get(newObjClass);
									
									List<?> createdObjs = newObjects.stream()
															  .map((no)->this.upgraderMap.get(newObjClass).upgrade(no).getUpgradedObject())
															  .toList();
									if (service instanceof MongoHistoriedObjectService<?, ?, ?>) {
										((MongoHistoriedObjectService) service).addBulk(
											oqmDbId,
											cs,
											createdObjs,
											this.getCoreApiInteractingEntity(),
											detail
										);
									} else if (service instanceof MongoObjectService<?, ?, ?>) {
										((MongoObjectService) service).addBulk(
											oqmDbId,
											cs,
											createdObjs
										);
									}
								});
							
							createdObjectResults.addAll(result.getUpgradeCreatedObjects());
						}
						
						typedCollection.findOneAndReplace(
							cs,
							eq("id", result.getUpgradedObject().getId()),
							result.getUpgradedObject()
						);
						//add upgrade event, if applicable
						MongoDbAwareService<?, ?, ?> objService = this.oqmDbServices.get(objectClass);
						if (objService instanceof MongoHistoriedObjectService<?, ?, ?>) {
							((MongoHistoriedObjectService<?, ?, ?>) objService).getHistoryService()
								.addHistoryFor(
									oqmDbId,
									cs,
									result.getUpgradedObject().getId(),
									this.getCoreApiInteractingEntity(),
									schemaUpgradeEvent
								);
						}
					}
				}
			} catch(JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		
		sw.stop();
		outputBuilder.timeTaken(Duration.of(sw.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS))
			.numObjectsUpgraded(numUpdated);
		
		return outputBuilder.build();
	}
	
	/**
	 * Handles upgrading a particular collection. Wrapper for the other method, getting specific details from the mongo service.
	 *
	 * @param dbCs
	 * @param oqmDb
	 * @param service
	 * @param <T>
	 *
	 * @return
	 * @throws ClassUpgraderNotFoundException
	 */
	private <T extends MainObject> CollectionUpgradeResult upgradeOqmCollection(String upgradeId, ClientSession dbCs, OqmMongoDatabase oqmDb, MongoDbAwareService<T, ?, ?> service)
		throws ClassUpgraderNotFoundException {
		log.info("Updating schema of oqm database service {} in ", service.getClass());
		String oqmDbId = oqmDb.getId().toHexString();
		//TODO:: hande upgrading history
		CollectionUpgradeResult result = this.upgradeOqmCollection(
			upgradeId,
			dbCs,
			oqmDbId,
			service.getDocumentCollection(oqmDbId),
			service.getTypedCollection(oqmDbId),
			service.getClazz()
		);
		
		log.info("DONE Updating schema of oqm database service {} in ", service.getClass());
		return result;
	}
	
	/**
	 * This method upgrades a particular oqm db to the latest schema.
	 * <p>
	 * This will iterate over each db aware mongo service/ collection.
	 *
	 * @param oqmDb The database to update the schema of.
	 *
	 * @return The result of the upgrades.
	 */
	private OqmDbUpgradeResult upgradeOqmDb(String upgradeId, OqmMongoDatabase oqmDb) {
		log.info("Updating schema of oqm database: {}", oqmDb);
		OqmDbUpgradeResult.Builder outputBuilder = OqmDbUpgradeResult.builder()
													   .dbName(oqmDb.getName());
		List<CollectionUpgradeResult> upgradeResults = new ArrayList<>();
		outputBuilder.collectionUpgradeResults(upgradeResults);
		
		StopWatch dbUpgradeTime = StopWatch.createStarted();
		ClientSession cs = null;
		try {
			for (List<MongoDbAwareService<?, ?, ?>> curServiceGroup : this.dbAwareUpgradeGroups) {
				List<CompletableFuture<CollectionUpgradeResult>> futures = new ArrayList<>();
				for (MongoDbAwareService<?, ?, ?> curService : curServiceGroup) {
					if (cs == null) {
						cs = curService.getNewClientSession(true);
					}
					ClientSession finalCs = cs;
					futures.add(
						CompletableFuture.supplyAsync(()->{
							return upgradeOqmCollection(upgradeId, finalCs, oqmDb, curService);
						})
					);
				}
				upgradeResults.addAll(
					futures.stream().map(CompletableFuture::join).toList()
				);
			}
			if (cs != null) {
				cs.commitTransaction();
			}
		} finally {
			if (cs != null) {
				cs.close();
			}
		}
		
		dbUpgradeTime.stop();
		outputBuilder.timeTaken(Duration.of(dbUpgradeTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));
		
		log.info("Done updating oqm database: {}", oqmDb);
		
		return outputBuilder.build();
	}
	
	/**
	 * This method is responsible for upgrading all the collections/ databases handled by the core api.
	 * <p>
	 * This method iterates through all oqm databases, and updates their schemas.
	 * <p>
	 * This is a multithreaded operation, handling each database in its own thread.
	 *
	 * @return The results of all the upgrades.
	 */
	public Optional<TotalUpgradeResult> updateSchema() {
		if (this.upgradeRan()) {
			return Optional.empty();
		}
		final String upgradeId = UUID.randomUUID().toString();
		log.info("Upgrading the schema held in the Database. Id: {}", upgradeId);
		
		
		TotalUpgradeResult.Builder totalResultBuilder = TotalUpgradeResult.builder()
															.id(upgradeId)
															.instanceId(this.instanceUuid);
		StopWatch totalTime = StopWatch.createStarted();
		
		//TODO:: migrate top levels
		
		
		List<CompletableFuture<OqmDbUpgradeResult>> resultMap = new ArrayList<>();
		for (OqmMongoDatabase curDb : this.oqmDatabaseService.listIterator()) {
			resultMap.add(CompletableFuture.supplyAsync(()->{
					return upgradeOqmDb(upgradeId, curDb);
				})
			);
		}
		totalResultBuilder.dbUpgradeResults(
			resultMap.stream().map((CompletableFuture<OqmDbUpgradeResult> future)->{
					try {
						return future.get();
					} catch(Throwable e) {
						throw new UpgradeFailedException("Failed to upgrade data in database.", e);
					}
				})
				.toList());
		totalTime.stop();
		totalResultBuilder.timeTaken(Duration.of(totalTime.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS));
		
		log.info("DONE upgrading the schema held in the Database.");
		this.startupUpgradeResult = totalResultBuilder.build();
		
		return this.getStartupUpgradeResult();
	}
}
