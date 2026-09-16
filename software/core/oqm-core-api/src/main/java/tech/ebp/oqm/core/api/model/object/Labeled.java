package tech.ebp.oqm.core.api.model.object;

import java.util.Collection;
import java.util.Optional;

public interface Labeled {
	
	static <T extends Labeled> Optional<T> findLabeledInSet(String labelToFind, Collection<T> collection){
		return collection.stream()
				   .filter((l)->l.getLabel().equals(labelToFind))
				   .findFirst();
	}
	
	public String getLabel();
}
