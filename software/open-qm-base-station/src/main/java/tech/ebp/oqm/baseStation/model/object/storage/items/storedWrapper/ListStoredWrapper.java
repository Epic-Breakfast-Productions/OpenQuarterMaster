package tech.ebp.oqm.baseStation.model.object.storage.items.storedWrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import tech.ebp.oqm.baseStation.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.baseStation.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.baseStation.model.object.storage.items.stored.Stored;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * Storage wrapper that holds a list of Stored.
 * TODO:: also implement queue?
 *
 * @param <S>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class ListStoredWrapper<S extends Stored>
	extends StoredWrapper<List<@Valid @NotNull S>, S>
	//	implements List<S> //only uncomment to get/override methods
{
	
	@Override
	@BsonIgnore
	public Stream<S> storedStream() {
		return this.stream();
	}
	
	@Override
	public long getNumStored() {
		return this.size();
	}
	
	@Override
	public void addStored(S stored) {
		this.add(stored);
	}
	
	@Override
	public S subtractStored(S stored) throws NotEnoughStoredException {
		boolean result = this.remove(stored);
		if (!result) {
			throw new NotEnoughStoredException("Stored to remove was not held.");
		}
		return stored;
	}
	
	@Override
	public List<ItemExpiryEvent> updateExpiredStates(ObjectId blockKey, Duration expiredWarningThreshold) {
		List<ItemExpiryEvent> output = new ArrayList<>();
		for (int i = 0; i < this.size(); i++) {
			Optional<ItemExpiryEvent> result = updateExpiredStateForStored(
				this.get(i),
				blockKey,
				expiredWarningThreshold
			);
			
			if (result.isPresent()) {
				output.add((ItemExpiryEvent) result.get().setIndex(i));
			}
		}
		return output;
	}
	
	// <editor-fold desc="List pass-through methods">
	//	@Override
	public int size() {
		return this.getStored().size();
	}
	
	@JsonIgnore
	//	@Override
	public boolean isEmpty() {
		return this.getStored().isEmpty();
	}
	
	//	@Override
	public boolean contains(Object o) {
		return this.getStored().contains(o);
	}
	
	//	@Override
	public Iterator<S> iterator() {
		return this.getStored().iterator();
	}
	
	//	@Override
	public Object[] toArray() {
		return this.getStored().toArray();
	}
	
	//	@Override
	public <T> T[] toArray(T[] ts) {
		//noinspection SuspiciousToArrayCall
		return this.getStored().toArray(ts);
	}
	
	//	@Override
	public boolean add(@NotNull S s) {
		boolean output = this.getStored().add(s);
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean remove(Object o) {
		boolean output = this.getStored().remove(o);
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean containsAll(Collection<?> collection) {
		//noinspection SlowListContainsAll
		boolean output = this.getStored().containsAll(collection);
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean addAll(Collection<? extends S> collection) {
		boolean output = this.getStored().addAll(collection);
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean addAll(int i, Collection<? extends S> collection) {
		boolean output = this.getStored().addAll(i, collection);
		
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean removeAll(Collection<?> collection) {
		boolean output = this.getStored().removeAll(collection);
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public boolean retainAll(Collection<?> collection) {
		boolean output = this.getStored().retainAll(collection);
		
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public void clear() {
		this.getStored().clear();
		this.recalcDerived();
	}
	
	//	@Override
	public S get(int i) {
		return this.getStored().get(i);
	}
	
	//	@Override
	public S set(int i, S s) {
		return this.getStored().set(i, s);
	}
	
	//	@Override
	public void add(int i, S s) {
		this.getStored().add(i, s);
		this.recalcDerived();
	}
	
	//	@Override
	public S remove(int i) {
		S output = this.getStored().remove(i);
		this.recalcDerived();
		return output;
	}
	
	//	@Override
	public int indexOf(Object o) {
		return this.getStored().indexOf(o);
	}
	
	//	@Override
	public int lastIndexOf(Object o) {
		return this.getStored().lastIndexOf(o);
	}
	
	//	@Override
	public ListIterator<S> listIterator() {
		return this.getStored().listIterator();
	}
	
	//	@Override
	public ListIterator<S> listIterator(int i) {
		return this.getStored().listIterator(i);
	}
	
	//	@Override
	public List<S> subList(int i, int i1) {
		return this.getStored().subList(i, i1);
	}
	
	//	@Override
	public void replaceAll(UnaryOperator<S> operator) {
		this.getStored().replaceAll(operator);
		this.recalcDerived();
	}
	
	//	@Override
	public void sort(Comparator<? super S> c) {
		this.getStored().sort(c);
	}
	
	//	@Override
	public Spliterator<S> spliterator() {
		return this.getStored().spliterator();
	}
	
	//	@Override
	public <T> T[] toArray(IntFunction<T[]> generator) {
		return this.getStored().toArray(generator);
	}
	
	//	@Override
	public boolean removeIf(Predicate<? super S> filter) {
		boolean output = this.getStored().removeIf(filter);
		
		if (output) {
			this.recalcDerived();
		}
		return output;
	}
	
	//	@Override
	public Stream<S> stream() {
		return this.getStored().stream();
	}
	
	//	@Override
	public Stream<S> parallelStream() {
		return this.getStored().parallelStream();
	}
	
	//	@Override
	public void forEach(Consumer<? super S> action) {
		this.getStored().forEach(action);
	}
	
	// </editor-fold>
}
