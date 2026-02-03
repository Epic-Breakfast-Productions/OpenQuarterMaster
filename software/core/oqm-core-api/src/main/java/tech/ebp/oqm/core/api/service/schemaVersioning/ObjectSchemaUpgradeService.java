package tech.ebp.oqm.core.api.service.schemaVersioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.TransactionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.ReturnDocument;
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
import tech.ebp.oqm.core.api.model.object.history.events.CreateEvent;
import tech.ebp.oqm.core.api.model.object.history.events.SchemaUpgradeEvent;
import tech.ebp.oqm.core.api.model.object.upgrade.CollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.HistoriedCollectionUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.ObjectUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.OqmDbUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.TotalUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeOverallCreatedObjectsResults;
import tech.ebp.oqm.core.api.service.mongo.*;
import tech.ebp.oqm.core.api.service.mongo.transactions.AppliedTransactionService;
import tech.ebp.oqm.core.api.service.mongo.utils.MongoSessionWrapper;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.ObjectSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.appliedTransaction.AppliedTransactionSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.checkout.CheckoutSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.historyEvent.HistoryEventSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.interactingEntity.InteractingEntitySchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.inventoryItem.InventoryItemSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.storageBlock.StorageBlockSchemaUpgrader;
import tech.ebp.oqm.core.api.service.schemaVersioning.upgraders.stored.StoredSchemaUpgrader;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmDatabaseService;
import tech.ebp.oqm.core.api.service.serviceState.db.OqmMongoDatabase;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.lt;
import static com.mongodb.client.model.Filters.type;

@ApplicationScoped
@Slf4j
public class ObjectSchemaUpgradeService {
	
	/** Map of upgraders to provide easy access to which upgraders for which object class. */
	private Map<Class<? extends MainObject>, ObjectSchemaUpgrader<?>> upgraderMap;
	/** The main oqm database service. */
	private OqmDatabaseService oqmDatabaseService;
	/** Map of classes to their corresponding oqm mongo db service, for easy access. */
	private Map<Class<? extends MainObject>, TopLevelMongoService<? extends MainObject, ?, ?>> topLevelServices;
	/** Map of classes to their corresponding oqm mongo db service, for easy access. */
	private Map<Class<? extends MainObject>, MongoDbAwareService<? extends MainObject, ?, ?>> oqmDbServices;
	/** Data structure for easy grouping of services that can/ should have their schema updated at the same time, and those groups in order. */
	private List<List<MongoDbAwareService<? extends MainObject, ?, ?>>> dbAwareUpgradeGroups;
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
		InteractingEntityService interactingEntityService,
		StorageBlockService storageBlockService,
		InventoryItemService inventoryItemService,
		StoredService storedService,
		ItemCheckoutService itemCheckoutService,
		AppliedTransactionService appliedTransactionService
	) {
		this.coreApiInteractingEntity = coreApiInteractingEntity;
		this.instanceUuid = instanceUuid;
		this.oqmDatabaseService = oqmDatabaseService;
		
		this.topLevelServices = new LinkedHashMap<>();
		Stream.of(
			interactingEntityService
		).forEachOrdered((service)->{
			this.topLevelServices.put(service.getClazz(), service);
		});
		
		//This insertion order here is the order of which these are each processed.
		this.dbAwareUpgradeGroups = List.of(
			List.of(
				storageBlockService
			),
			List.of(
				inventoryItemService
			),
			List.of(
				storedService,
				itemCheckoutService,
				appliedTransactionService
			)
		);
		this.oqmDbServices = this.dbAwareUpgradeGroups
								 .stream()
								 .flatMap(List::stream)
								 .reduce(
									 new HashMap<>(),
									 (map, element)->{
										 map.put(element.getClazz(), element);
										 return map;
									 },
									 (map1, map2)->{
										 map1.putAll(map2);
										 return map1;
									 }
								 );
		
		this.upgraderMap = Stream.of(
			new HistoryEventSchemaUpgrader(),
			new InteractingEntitySchemaUpgrader(),
			new StorageBlockSchemaUpgrader(),
			new InventoryItemSchemaUpgrader(),
			new StoredSchemaUpgrader(),
			new CheckoutSchemaUpgrader(),
			new AppliedTransactionSchemaUpgrader()
		).reduce(
			new HashMap<>(),
			(map, element)->{
				map.put(element.getObjClass(), element);
				return map;
			},
			(map1, map2)->{
				map1.putAll(map2);
				return map1;
			}
		);
	}
	
	public Optional<TotalUpgradeResult> getStartupUpgradeResult() {
		return Optional.ofNullable(this.startupUpgradeResult);
	}
	
	public boolean upgradeRan() {
		return this.startupUpgradeResult != null;
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
	
	private <T extends MainObject> void processCreatedObjects(
		ClientSession cs,
		String oqmDbId,
		Class<T> newObjClass,
		List<ObjectNode> newObjects,
		FromSchemaUpgradeDetail detail
	) {
		MongoDbAwareService<T, ?, ?> service = (MongoDbAwareService<T, ?, ?>) this.oqmDbServices.get(newObjClass);
		ObjectSchemaUpgrader<T> upgrader = (ObjectSchemaUpgrader<T>) this.upgraderMap.get(newObjClass);
		
		if (service == null) {
			throw new IllegalStateException("Service for class not found: " + newObjClass.getName());
		}
		if (upgrader == null) {
			throw new IllegalStateException("Upgrader for class not found: " + newObjClass.getName());
		}
		
		List<T> createdObjs = newObjects.stream()
								  .map((no)->upgrader.upgrade(no).getUpgradedObject())
								  .filter(Optional::isPresent)
								  .map(Optional::get)
								  .toList();
		
		createdObjs.stream()
			.forEach((curCreated)->{
					ObjectId newId = service.getTypedCollection(oqmDbId)
										 .insertOne(cs, curCreated).getInsertedId().asObjectId().getValue();
					
					if (service instanceof MongoHistoriedObjectService) {
						((MongoHistoriedObjectService<T, ?, ?>) service).getHistoryService()
							.addHistoryFor(
								oqmDbId,
								cs,
								newId,
								this.getCoreApiInteractingEntity(),
								CreateEvent.builder()
									.objectId(newId)
									.details(MongoHistoriedObjectService.detailListToMap(detail))
									.build()
							);
					}
				}
			);
	}
	
	private void processCreatedObjects(
		String upgradeId,
		ClientSession cs,
		String oqmDbId,
		Class<?> objectClass,
		ObjectId upgradedObjectId,
		ObjectId schemaUpgradeEventId,
		UpgradeCreatedObjectsResults createdObjectsResults
	) {
		FromSchemaUpgradeDetail detail = new FromSchemaUpgradeDetail(
			upgradeId,
			objectClass.getSimpleName(),
			upgradedObjectId,
			schemaUpgradeEventId
		);
		createdObjectsResults
			.forEach((newObjClass, newObjects)->{
				this.processCreatedObjects(
					cs,
					oqmDbId,
					newObjClass,
					newObjects,
					detail
				);
			});
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
		int curSchemaVersion,
		String oqmDbId,
		MongoCollection<Document> documentCollection,
		MongoCollection<T> typedCollection,
		Class<T> objectClass,
		CollectionUpgradeResult.CollectionUpgradeResultBuilder<?, ?> outputBuilder
	) throws ClassUpgraderNotFoundException {
		ObjectSchemaUpgrader<T> objectVersionBumper = this.getUpgrader(objectClass);
		outputBuilder.collectionClass(objectClass);
		outputBuilder.collectionName(documentCollection.getNamespace().getCollectionName());
		
		UpgradeOverallCreatedObjectsResults createdObjectResults = new UpgradeOverallCreatedObjectsResults();
		outputBuilder.createdObjects(createdObjectResults);
		
		StopWatch sw = StopWatch.createStarted();
		long numUpdated = 0;
		long numNotUpgraded = 0;
		long numDeleted = 0;
		
		if (objectVersionBumper.upgradesAvailable()) {
			try (
				MongoCursor<Document> it = documentCollection
											   .find(lt("schemaVersion", curSchemaVersion)
											   )
											   .cursor()
			) {
				while (it.hasNext()) {
					Document doc = it.next();
					ObjectUpgradeResult<T> result = objectVersionBumper.upgrade(doc);
					Optional<T> upgradedObject = result.getUpgradedObject();
					SchemaUpgradeEvent schemaUpgradeEvent = SchemaUpgradeEvent.builder()
																.upgradeId(upgradeId)
																.id(new ObjectId())
																.fromVersion(result.getOldVersion())
																.toVersion(
																	upgradedObject.map(Versionable::getSchemaVersion).orElse(-1)
																)
																.build();
					
					if (result.isDelObj()) {
						log.info("Deleting object with id {} in collection {}", doc.getObjectId("_id"), documentCollection.getNamespace().getCollectionName());
						typedCollection.deleteOne(cs, eq("_id", result.getObjectId()));
						numDeleted++;
					} else if (!result.wasUpgraded()) {
						numNotUpgraded++;
					} else {
						numUpdated++;
						
						
						log.info("Updating object db entry with id {} in collection {}", doc.getObjectId("_id"), documentCollection.getNamespace().getCollectionName());
						T previous = typedCollection.findOneAndReplace(
							cs,
							eq("_id", result.getObjectId()),
							upgradedObject.orElseThrow(()->new IllegalStateException("Upgraded object was null, and not set to delete!")),
							new FindOneAndReplaceOptions()
								//get version of object after we update, otherwise it fails to map to our object.
								.returnDocument(ReturnDocument.AFTER)
						);
						if (previous == null) {
							throw new RuntimeException("Previous object was not upgraded...");
						}
						
						//TODO:: support top level collections to do these things
						if (oqmDbId != null) {
							//add upgrade event, if applicable
							MongoDbAwareService<?, ?, ?> objService = this.oqmDbServices.get(objectClass);
							if (objService instanceof MongoHistoriedObjectService<?, ?, ?>) {
								((MongoHistoriedObjectService<?, ?, ?>) objService).getHistoryService()
									.addHistoryFor(
										oqmDbId,
										cs,
										result.getObjectId(),
										this.getCoreApiInteractingEntity(),
										schemaUpgradeEvent
									);
							}
						}
					}
					
					//TODO:: support top level collections to do these things
					if (oqmDbId != null) {
						if (result.hasUpgradedCreatedObjects()) {
							this.processCreatedObjects(
								upgradeId,
								cs,
								oqmDbId,
								objectClass,
								result.getObjectId(),
								schemaUpgradeEvent.getId(),
								result.getUpgradeCreatedObjects()
							);
							createdObjectResults.addAll(result.getUpgradeCreatedObjects());
						}
					}
				}
			} catch(JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}
		
		sw.stop();
		outputBuilder.timeTaken(Duration.of(sw.getTime(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS))
			.numObjectsUpgraded(numUpdated)
			.numObjectsNotUpgraded(numNotUpgraded)
			.numObjectsDeleted(numDeleted);
		
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
		throws ClassUpgraderNotFoundException, ExecutionException, InterruptedException {
		boolean historiedService = service instanceof MongoHistoriedObjectService;
		log.info("Updating schema of oqm database service {} in db {}", service.getClass(), oqmDb.getName());
		String oqmDbId = oqmDb.getId().toHexString();
		//TODO:: hande upgrading history
		CollectionUpgradeResult.CollectionUpgradeResultBuilder<?, ?> outputBuilder = historiedService ? HistoriedCollectionUpgradeResult.builder() : CollectionUpgradeResult.builder();
		CompletableFuture<CollectionUpgradeResult> collectionFuture = CompletableFuture.supplyAsync(()->{
			return this.upgradeOqmCollection(
				upgradeId,
				dbCs,
				service.getCurrentSchemaVersion(),
				oqmDbId,
				service.getDocumentCollection(oqmDbId),
				service.getTypedCollection(oqmDbId),
				service.getClazz(),
				outputBuilder
			);
		});
		
		Optional<CompletableFuture<CollectionUpgradeResult>> histCollOp = Optional.empty();
		if (historiedService) {
			log.info("Service is historied, processing history events.");
			histCollOp = Optional.of(
				CompletableFuture.supplyAsync(()->{
					MongoHistoryService<T> histService = ((MongoHistoriedObjectService<T, ?, ?>) service).getHistoryService();
					return this.upgradeOqmCollection(
						upgradeId,
						dbCs,
						histService.getCurrentSchemaVersion(),
						oqmDbId,
						histService.getDocumentCollection(oqmDbId),
						histService.getTypedCollection(oqmDbId),
						histService.getClazz(),
						CollectionUpgradeResult.builder()
					);
				})
			);
		}
		
		collectionFuture.get();
		
		if (histCollOp.isPresent()) {
			((HistoriedCollectionUpgradeResult.HistoriedCollectionUpgradeResultBuilder<?, ?>) outputBuilder).historyCollectionUpgradeResult(histCollOp.get().get());
		}
		
		log.info("DONE Updating schema of oqm database service {} in ", service.getClass());
		return outputBuilder.build();
	}
	
	private <T extends MainObject> CollectionUpgradeResult upgradeOqmCollection(String upgradeId, ClientSession dbCs, TopLevelMongoService<T, ?, ?> service) {
		log.info("Updating schema of top level oqm database service {}", service.getClass());
		
		CollectionUpgradeResult result = this.upgradeOqmCollection(
			upgradeId,
			dbCs,
			service.getCurrentSchemaVersion(),
			null,
			service.getDocumentCollection(),
			service.getTypedCollection(),
			service.getClazz(),
			CollectionUpgradeResult.builder()
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
		OqmDbUpgradeResult.OqmDbUpgradeResultBuilder outputBuilder = OqmDbUpgradeResult.builder()
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
							try {
								return upgradeOqmCollection(upgradeId, finalCs, oqmDb, curService);
							} catch(ExecutionException | InterruptedException e) {
								throw new UpgradeFailedException(e);
							}
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
	public Optional<TotalUpgradeResult> updateSchema(boolean force) {
		if (!force && this.upgradeRan()) {
			log.info("Already ran schema update. Not updating oqm database schema.");
			return Optional.empty();
		}
		final String upgradeId = UUID.randomUUID().toString();
		log.info("Upgrading the schema held in the Database. Id: {}", upgradeId);
		
		
		AtomicReference<TotalUpgradeResult> result = new AtomicReference<>();
		try (MongoSessionWrapper csw = new MongoSessionWrapper(this.oqmDatabaseService)) {
			csw.runTransaction(true, (ClientSession cs)->{
				TotalUpgradeResult.TotalUpgradeResultBuilder totalResultBuilder = TotalUpgradeResult.builder()
																	.id(upgradeId)
																	.instanceId(this.instanceUuid);
				StopWatch totalTime = StopWatch.createStarted();
				{//top level migration
					log.info("Upgrading top level collections.");
					List<CollectionUpgradeResult> topLevelResults = new ArrayList<>();
					
					//TODO:: session wrapper for all things not just one then the other
					for (TopLevelMongoService<?, ?, ?> curTopLevelService : this.topLevelServices.values()) {
						topLevelResults.add(this.upgradeOqmCollection(upgradeId, cs, curTopLevelService));
					}
					
					
					totalResultBuilder.topLevelUpgradeResults(topLevelResults);
					log.info("DONE upgrading top level results.");
				}
				
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
				
				cs.commitTransaction();
				
				totalTime.stop();
				totalResultBuilder.timeTaken(totalTime.getDuration());
				result.set(totalResultBuilder.build());
				
				log.info("DONE upgrading the schema held in the Database.");
				log.info("Running post upgrade tasks.");
				
				cs.startTransaction();
				
				TotalUpgradeResult innerResult = result.get();
				if (innerResult.wasUpgraded()) {
					innerResult.getTopLevelUpgradeResults().stream()
						.filter(CollectionUpgradeResult::wasUpgraded)
						.forEach((CollectionUpgradeResult curResult)->{
							log.info("Running post upgrade tasks for top level collection: {}", curResult.getCollectionName());
							topLevelServices.get(curResult.getCollectionClass()).runPostUpgrade(cs, curResult);
						});
					innerResult.getDbUpgradeResults().stream()
						.filter(OqmDbUpgradeResult::wasUpgraded)
						.forEach((OqmDbUpgradeResult curDbResult)->{
							curDbResult.getCollectionUpgradeResults()
								.stream()
								.filter(CollectionUpgradeResult::wasUpgraded)
								.forEach((CollectionUpgradeResult curResult)->{
									log.info("Running post upgrade tasks for collection: {} / {}", curResult.getCollectionName(), curResult.getCollectionClass());
									MongoDbAwareService<?, ?, ?> service = this.oqmDbServices.get(curResult.getCollectionClass());
									service.runPostUpgrade(curDbResult.getDbName(), cs, curResult);
									
									if (service instanceof MongoHistoriedObjectService) {
										((MongoHistoriedObjectService) service).getHistoryService().runPostUpgrade(curDbResult.getDbName(), cs, curResult);
									}
								});
						});
					
				} else {
					log.info("No object upgraded, no reason to run post upgrade tasks.");
				}
				
				log.info("DONE running post-upgrade tasks.");
			});
		}
		
		this.startupUpgradeResult = result.get();
		
		log.info("DONE running post-upgrade tasks.");
		return this.getStartupUpgradeResult();
	}
	
	public Optional<TotalUpgradeResult> updateSchema() {
		return this.updateSchema(false);
	}
}
