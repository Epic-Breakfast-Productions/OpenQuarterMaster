package tech.ebp.oqm.core.api.model.object.upgrade;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;
import tech.ebp.oqm.core.api.exception.ClassUpgraderNotFoundException;
import tech.ebp.oqm.core.api.exception.UpgradeFailedException;
import tech.ebp.oqm.core.api.exception.VersionBumperListIncontiguousException;
import tech.ebp.oqm.core.api.model.object.ObjectUtils;
import tech.ebp.oqm.core.api.model.object.Versionable;
import tech.ebp.oqm.core.api.model.object.storage.items.AmountItem;

import java.time.Duration;
import java.util.*;


/**
 * Handles object upgrading for a particular object.
 * @param <T>
 */
@Slf4j
public abstract class ObjectUpgrader<T extends Versionable> {

	private static Map<Class<?>, ObjectUpgrader<?>> instanceMap = new HashMap<>();

	public static void clearInstanceMap(){
		instanceMap = new HashMap<>();
	}

	public static <C extends Versionable> ObjectUpgrader<C> getInstanceForClass(@NonNull Class<C> clazz) throws ClassUpgraderNotFoundException {
		ObjectUpgrader<C> output = null;

		if(instanceMap.containsKey(clazz)){
			output = (ObjectUpgrader<C>) instanceMap.get(clazz);
		} else if(clazz.equals(AmountItem.class)){
			//output = ; TODO
			instanceMap.put(clazz, output);
		}
		//TODO:: rest

		if(output == null) {
			throw new ClassUpgraderNotFoundException(clazz);
		}
		return output;
	}

	@Getter
	private final SortedSet<ObjectVersionBumper<T>> versionBumpers;
	@Getter
	private final Class<T> objClass;

	protected ObjectUpgrader(Class<T> objClass, SortedSet<ObjectVersionBumper<T>> versionBumpers) throws VersionBumperListIncontiguousException {
		this.versionBumpers = versionBumpers;
		this.objClass = objClass;

		//check that the set is contiguous
		int lastVersion = 1;
		for(ObjectVersionBumper<T> cur : this.versionBumpers){
			if((lastVersion + 1) != cur.getBumperTo()){
				throw new VersionBumperListIncontiguousException(lastVersion, this.objClass);
			}
			lastVersion = cur.getBumperTo();
		}
	}

	protected ObjectUpgrader(Class<T> objClass, ObjectVersionBumper<T> ... versionBumpers) throws VersionBumperListIncontiguousException {
		this(
			objClass,
			new TreeSet<>(Arrays.stream(versionBumpers).toList())
		);
	}

	protected Iterator<ObjectVersionBumper<T>> getIteratorAtVersion(int curObjVersion){
		int curVersionTo = curObjVersion + 1;

		LinkedList<ObjectVersionBumper<T>> bumpers = new LinkedList<>(this.versionBumpers);

		while(bumpers.getFirst().getBumperTo() < curVersionTo){
			bumpers.removeFirst();
		}

		return bumpers.iterator();
	}


	public UpgradeResult<T> upgrade(JsonNode oldObj){
		int curVersion = oldObj.get("schemaVersion").asInt(1);
		UpgradeResult.Builder<T> resultBuilder = UpgradeResult.builder();
		resultBuilder.oldVersion(curVersion);

		JsonNode upgradedJson = oldObj.deepCopy();

		StopWatch sw = StopWatch.createStarted();
		Iterator<ObjectVersionBumper<T>> it = getIteratorAtVersion(curVersion);
		while (it.hasNext()){
			ObjectVersionBumper<T> curBumper = it.next();

			upgradedJson = curBumper.bumpObject(upgradedJson);
		}
		T upgradedObj = null;
		try {
			upgradedObj = ObjectUtils.OBJECT_MAPPER.treeToValue(upgradedJson, this.objClass);
		} catch(JsonProcessingException e) {
			throw new UpgradeFailedException(e, this.getObjClass());
		}
		sw.stop();

		resultBuilder.upgradedObject(upgradedObj);
		resultBuilder.timeToUpgrade(Duration.ofMillis(sw.getTime()));

		return resultBuilder.build();
	}

	public UpgradeResult<T> upgrade(Document oldObj) throws JsonProcessingException {
		return this.upgrade(
			ObjectUtils.OBJECT_MAPPER.readTree(
				oldObj.toJson(
					JsonWriterSettings.builder()
						.build()
				)
			)
		);
	}
}
