package tech.ebp.oqm.core.api.model.object.storage.items.identifiers.generation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Generated;
import tech.ebp.oqm.core.api.model.object.storage.items.identifiers.Identifier;

import java.util.LinkedHashSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IdGenResult<T extends Identifier & Generated> {
	
	private LinkedHashSet<T> generatedIds = new LinkedHashSet<>();
	
	public boolean addGeneratedId(T id) {
		return this.generatedIds.add(id);
	}
}
