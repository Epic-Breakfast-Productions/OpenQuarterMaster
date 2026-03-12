package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated.Generated;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.GenericIdentifier;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.types.generated.GeneratedIdentifier;

import java.util.LinkedHashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdGenResult {
	
	private LinkedHashSet<GeneratedIdentifier> generatedIds = new LinkedHashSet<>();
	
	public boolean addGeneratedId(GeneratedIdentifier id) {
		return this.generatedIds.add(id);
	}
}
