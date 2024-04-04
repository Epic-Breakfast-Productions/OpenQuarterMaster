package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper;

import tech.ebp.oqm.core.api.model.object.storage.items.exception.StoredNotFoundException;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

import java.util.Iterator;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class MultiStoredWrapper<T, S extends Stored> extends StoredWrapper<T, S> {
	
	public S getStoredWithId(UUID storedId) throws StoredNotFoundException {
		return this.parallelStream().filter(
			Stored.getHasIdPredicate(storedId)
		).findFirst().orElseThrow(()->new StoredNotFoundException("No stored found with id " + storedId));
	}
	
	public boolean hasStoredWithId(UUID storedId){
		return this.parallelStream().anyMatch(
			Stored.getHasIdPredicate(storedId)
		);
	}
	
	
	
	public abstract Iterator<S> iterator();
	
	public abstract Stream<S> stream();
	
	public abstract Stream<S> parallelStream();
	
	public abstract void forEach(Consumer<? super S> action);
}
