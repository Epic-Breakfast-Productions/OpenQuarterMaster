package tech.ebp.oqm.core.api.service.schemaVersioning.upgraders;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.exception.VersionBumperListIncontiguousException;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.upgrade.ObjectUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.SingleUpgradeResult;
import tech.ebp.oqm.core.api.model.object.upgrade.UpgradeCreatedObjectsResults;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Handles object upgrading for a particular object.
 * @param <T>
 */
@Slf4j
public abstract class ObjectSchemaUpgrader<T extends Versionable> {

	@Getter
	private final SortedSet<ObjectSchemaVersionBumper<T>> versionBumpers;
	@Getter
	private final Class<T> objClass;
	private final Map<Integer, LinkedList<ObjectSchemaVersionBumper<T>>> bumperListCacheMap = new ConcurrentHashMap<>();

	protected ObjectSchemaUpgrader(Class<T> objClass, SortedSet<ObjectSchemaVersionBumper<T>> versionBumpers) throws VersionBumperListIncontiguousException {
		this.versionBumpers = versionBumpers;
		this.objClass = objClass;

		//check that the set is contiguous
		int lastVersion = 1;
		for(ObjectSchemaVersionBumper<T> cur : this.versionBumpers){
			if((lastVersion + 1) != cur.getBumperTo()){
				throw new VersionBumperListIncontiguousException(lastVersion, this.objClass);
			}
			lastVersion = cur.getBumperTo();
		}
	}

	protected ObjectSchemaUpgrader(Class<T> objClass, ObjectSchemaVersionBumper<T>... versionBumpers) throws VersionBumperListIncontiguousException {
		this(
			objClass,
			new TreeSet<>(Arrays.stream(versionBumpers).toList())
		);
	}

	protected LinkedList<ObjectSchemaVersionBumper<T>> getBumperFromCache(int versionTo){
		if(!this.bumperListCacheMap.containsKey(versionTo)){
			return null;
		}
		return new LinkedList<>(this.bumperListCacheMap.get(versionTo));
	}

	protected Iterator<ObjectSchemaVersionBumper<T>> getBumperIteratorAtVersion(int curObjVersion){
		int curVersionTo = curObjVersion + 1;

		LinkedList<ObjectSchemaVersionBumper<T>> bumpers = this.getBumperFromCache(curVersionTo);

		if(bumpers != null){
			return bumpers.iterator();
		}

		bumpers = new LinkedList<>(this.versionBumpers);

		while(!bumpers.isEmpty() && bumpers.getFirst().getBumperTo() < curVersionTo){
			bumpers.removeFirst();
		}

		this.bumperListCacheMap.put(curVersionTo, bumpers);
		bumpers = this.getBumperFromCache(curVersionTo);

		return bumpers.iterator();
	}
	
	/**
	 * Performs minor tweaks to the resulting upgraded object json.
	 *
	 * @param oldObj The upgraded object json we are working with
	 * @return oldObj
	 */
	protected ObjectNode adjustUpgradedObj(ObjectNode oldObj){
		if(oldObj.has("_t")) {
			oldObj.remove("_t");
		}
		return oldObj;
	}
	
	private ObjectId getObjectId(ObjectNode oldObj){
		if(oldObj.has("id")) {
			return new ObjectId(oldObj.get("id").asText());
		}
		if(oldObj.has("_id")) {
			return new ObjectId(oldObj.get("_id").asText());
		}
		throw new IllegalArgumentException(
			"Object given must have id field or _id field."
		);
	}

	public ObjectUpgradeResult<T> upgrade(ObjectNode oldObj){
		int curVersion = oldObj.get("schemaVersion").asInt(1);
		ObjectUpgradeResult.ObjectUpgradeResultBuilder<T> resultBuilder = ObjectUpgradeResult.builder();
		resultBuilder.objectId(this.getObjectId(oldObj));
		resultBuilder.oldVersion(curVersion);
		UpgradeCreatedObjectsResults upgradeCreatedObjects = new UpgradeCreatedObjectsResults();
		resultBuilder.upgradeCreatedObjects(upgradeCreatedObjects);

		ObjectNode upgradedJson = oldObj.deepCopy();
		
		log.debug("Initial object (version {}): {}", curVersion, upgradedJson);

		//Iterate and process upgrades for each necessary bump
		StopWatch sw = StopWatch.createStarted();
		Iterator<ObjectSchemaVersionBumper<T>> it = getBumperIteratorAtVersion(curVersion);
		boolean delObj = false;
		while (it.hasNext()){
			ObjectSchemaVersionBumper<T> curBumper = it.next();

			//Process bump
			SingleUpgradeResult upgradeResult = curBumper.bumpObject(upgradedJson);
			upgradedJson = upgradeResult.getUpgradedObject();
			
			//Process created objects during the bump
			upgradeCreatedObjects.addAll(upgradeResult.getCreatedObjects());
			
			if(upgradeResult.isDelObj()){
				log.debug("Object was deleted during upgrade to schema version {}", curBumper.getBumperTo());
				delObj = true;
				resultBuilder.delObj(true);
				break;
			}
		}
		
		this.adjustUpgradedObj(upgradedJson);
		
		log.debug("Upgraded object: {}", upgradedJson);
		
		if(delObj){
			resultBuilder.upgradedObject(Optional.empty());
		} else {
			// Get end result object from resulting bumped json
			T upgradedObj = null;
			try {
				upgradedObj = ObjectUtils.OBJECT_MAPPER.treeToValue(upgradedJson, this.objClass);
			} catch(JsonProcessingException e) {
				log.error("Failed to deserialize upgraded object of class {}: {}", this.objClass, upgradedJson.toPrettyString(), e);
				throw new UpgradeFailedException(e, this.getObjClass());
			}
			log.debug("Object successfully deserialized: {}", upgradedObj);
			resultBuilder.upgradedObject(Optional.of(upgradedObj));
		}
		sw.stop();
		
		resultBuilder.timeTaken(sw.getDuration());

		return resultBuilder.build();
	}

	public ObjectUpgradeResult<T> upgrade(Document oldObjDoc) throws JsonProcessingException {
		ObjectNode oldObj = (ObjectNode) ObjectUtils.OBJECT_MAPPER.readTree(
			oldObjDoc.toJson(
				JsonWriterSettings.builder()
					.build()
			)
		);
		
		if(oldObj.has("_id")) {
			oldObj.put("id", oldObj.get("_id").get("$oid").asText());
			oldObj.remove("_id");
		}
		oldObj.remove("storedType_mongo");
		
		return this.upgrade(oldObj);
	}

	public boolean upgradesAvailable(){
		return !this.versionBumpers.isEmpty();
	}
}
