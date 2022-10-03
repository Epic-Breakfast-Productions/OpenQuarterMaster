package tech.ebp.oqm.lib.core.object.storage.items.storedWrapper;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.ebp.oqm.lib.core.object.storage.items.stored.Stored;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class MapStoredWrapper<S extends Stored> extends StoredWrapper<Map<String, @NotNull S>, S> implements Map<String, S> {
	
	public MapStoredWrapper() {
		super(new HashMap<>());
	}
	
	// <editor-fold desc="Map pass-through methods">
	@Override
	public int size() {
		return this.getStored().size();
	}
	
	@Override
	public boolean isEmpty() {
		return this.getStored().isEmpty();
	}
	
	@Override
	public boolean containsKey(Object o) {
		return this.getStored().containsKey(o);
	}
	
	@Override
	public boolean containsValue(Object o) {
		return this.getStored().containsValue(o);
	}
	
	@Override
	public S get(Object o) {
		return this.getStored().get(o);
	}
	
	@Override
	public S put(String s, S s2) {
		return this.getStored().put(s, s2);
	}
	
	@Override
	public S remove(Object o) {
		return this.getStored().remove(o);
	}
	
	@Override
	public void putAll(Map<? extends String, ? extends S> map) {
		this.getStored().putAll(map);
	}
	
	@Override
	public void clear() {
		this.getStored().clear();
	}
	
	@Override
	public Set<String> keySet() {
		return this.getStored().keySet();
	}
	
	@Override
	public Collection<S> values() {
		return this.getStored().values();
	}
	
	@Override
	public Set<Entry<String, S>> entrySet() {
		return this.getStored().entrySet();
	}
	// </editor-fold>
}
