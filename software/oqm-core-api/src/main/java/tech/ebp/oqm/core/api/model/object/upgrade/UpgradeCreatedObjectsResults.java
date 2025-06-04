package tech.ebp.oqm.core.api.model.object.upgrade;


import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeCreatedObjectsResults extends HashMap<Class<?>, List<ObjectNode>> {
	
	public UpgradeCreatedObjectsResults(Map<? extends Class<?>, List<ObjectNode>> m) {
		super(m);
	}
	
	public UpgradeCreatedObjectsResults() {
	}
	
	public UpgradeCreatedObjectsResults(int initialCapacity) {
		super(initialCapacity);
	}
	
	public UpgradeCreatedObjectsResults(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	public void addAll(Map<Class<?>, List<ObjectNode>> upgradeCreatedObjects) {
		upgradeCreatedObjects.forEach((key, value)->{
			if(!this.containsKey(key)) {
				this.put(key, new ArrayList<>());
			}
			this.get(key).addAll(value);
		});
	}
}
