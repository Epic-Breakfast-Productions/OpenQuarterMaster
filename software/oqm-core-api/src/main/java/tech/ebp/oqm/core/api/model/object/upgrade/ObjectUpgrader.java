package tech.ebp.oqm.core.api.model.object.upgrade;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import tech.ebp.oqm.core.api.model.object.Versionable;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class ObjectUpgrader<T extends Versionable> {
	@Getter
	private final SortedSet<ObjectVersionBumper<T>> versionBumpers;
	
	private final Class<T> objClass;
	
	public ObjectUpgrader(Class<T> objClass, SortedSet<ObjectVersionBumper<T>> versionBumpers) {
		this.versionBumpers = versionBumpers;
		this.objClass = objClass;
	}
	
	public ObjectUpgrader(Class<T> objClass, ObjectVersionBumper<T> ... versionBumpers){
		this(
			objClass,
			new TreeSet<>(Arrays.stream(versionBumpers).toList())
		);
	}
	
	
	public UpgradeResult<T> upgrade(JsonNode oldObj){
		//TODO
		return null;
	}
}
