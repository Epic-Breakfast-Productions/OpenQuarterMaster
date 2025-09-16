package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniqueIdGenResult {
	
	private LinkedHashSet<String> generatedIds = new LinkedHashSet<>();
	
	public boolean addGeneratedId(String id) {
		return this.generatedIds.add(id);
	}
}
