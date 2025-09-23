package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.unique;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UniqueIdGenResult {
	
	private LinkedHashSet<GeneratedUniqueId> generatedIds = new LinkedHashSet<>();
	
	public boolean addGeneratedId(GeneratedUniqueId id) {
		return this.generatedIds.add(id);
	}
}
