package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Storage wrapper that holds a list of Stored.
 * TODO:: also implement queue?
 *
 * @param <S>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class ListStoredWrapper<S extends Stored> extends StoredWrapper<List<@NotNull S>, S> implements List<S> {
	
	public ListStoredWrapper() {
		super(new ArrayList<>());
	}
	
	// <editor-fold desc="List pass-through methods">
	@Override
	public int size() {
		return this.getStored().size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.getStored().isEmpty();
	}
	
	@Override
	public boolean contains(Object o) {
		return this.getStored().contains(o);
	}
	
	@Override
	public Iterator<S> iterator() {
		return this.getStored().iterator();
	}
	
	@Override
	public Object[] toArray() {
		return this.getStored().toArray();
	}
	
	@Override
	public <T> T[] toArray(T[] ts) {
		//noinspection SuspiciousToArrayCall
		return this.getStored().toArray(ts);
	}
	
	@Override
	public boolean add(S s) {
		return this.getStored().add(s);
	}
	
	@Override
	public boolean remove(Object o) {
		return this.getStored().remove(o);
	}
	
	@Override
	public boolean containsAll(Collection<?> collection) {
		//noinspection SlowListContainsAll
		return this.getStored().containsAll(collection);
	}
	
	@Override
	public boolean addAll(Collection<? extends S> collection) {
		return this.getStored().addAll(collection);
	}
	
	@Override
	public boolean addAll(int i, Collection<? extends S> collection) {
		return this.getStored().addAll(i, collection);
	}
	
	@Override
	public boolean removeAll(Collection<?> collection) {
		return this.getStored().removeAll(collection);
	}
	
	@Override
	public boolean retainAll(Collection<?> collection) {
		return this.getStored().retainAll(collection);
	}
	
	@Override
	public void clear() {
		this.getStored().clear();
	}
	
	@Override
	public S get(int i) {
		return this.getStored().get(i);
	}
	
	@Override
	public S set(int i, S s) {
		return this.getStored().set(i, s);
	}
	
	@Override
	public void add(int i, S s) {
		this.getStored().add(i, s);
	}
	
	@Override
	public S remove(int i) {
		return this.getStored().remove(i);
	}
	
	@Override
	public int indexOf(Object o) {
		return this.getStored().indexOf(o);
	}
	
	@Override
	public int lastIndexOf(Object o) {
		return this.getStored().lastIndexOf(o);
	}
	
	@Override
	public ListIterator<S> listIterator() {
		return this.getStored().listIterator();
	}
	
	@Override
	public ListIterator<S> listIterator(int i) {
		return this.getStored().listIterator(i);
	}
	
	@Override
	public List<S> subList(int i, int i1) {
		return this.getStored().subList(i, i1);
	}
	// </editor-fold>
}
