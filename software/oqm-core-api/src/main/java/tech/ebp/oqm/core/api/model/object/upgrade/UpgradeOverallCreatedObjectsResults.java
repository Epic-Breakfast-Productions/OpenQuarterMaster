package tech.ebp.oqm.core.api.model.object.upgrade;


import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpgradeOverallCreatedObjectsResults extends HashMap<Class<?>, Long> {
	
	public UpgradeOverallCreatedObjectsResults(Map<? extends Class<?>, Long> m) {
		super(m);
	}
	
	public UpgradeOverallCreatedObjectsResults() {
	}
	
	public UpgradeOverallCreatedObjectsResults(int initialCapacity) {
		super(initialCapacity);
	}
	
	public UpgradeOverallCreatedObjectsResults(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}
	
	public void addAll(Map<Class<?>, List<ObjectNode>> upgradeCreatedObjects) {
		upgradeCreatedObjects.forEach((key, value)->{
			this.put(
				key,
				this.getOrDefault(key, 0L) + value.size()
			);
		});
	}
}
