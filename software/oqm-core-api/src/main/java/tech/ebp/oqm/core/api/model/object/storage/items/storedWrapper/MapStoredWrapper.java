package tech.ebp.oqm.core.api.model.object.storage.items.storedWrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;
import tech.ebp.oqm.core.api.model.object.history.events.item.expiry.ItemExpiryEvent;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.AlreadyStoredException;
import tech.ebp.oqm.core.api.model.object.storage.items.exception.NotEnoughStoredException;
import tech.ebp.oqm.core.api.model.object.storage.items.stored.Stored;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class MapStoredWrapper<S extends Stored>
	extends MultiStoredWrapper<Map<String, @NotNull S>, S>
	//	implements Map<String, S> //Used for overriding passthrough methods
{
	
	//	public MapStoredWrapper() {
	//		super(new HashMap<>());
	//	}
	
	
	@Override
	@BsonIgnore
	public Stream<S> storedStream() {
		return this.values().stream();
	}
	
	@Override
	public long getNumStored() {
		return this.size();
	}
	
	//TODO:: make an interface for stored objs to have identifiers to use here?
	protected void addStored(String identifier, S stored) throws AlreadyStoredException {
		if (this.containsKey(identifier)) {
			throw new AlreadyStoredException("Identifier already present: " + identifier);
		}
		this.put(identifier, stored);
	}
	
	public S subtractStored(String identifier) throws NotEnoughStoredException {
		S result = this.remove(identifier);
		if (result == null) {
			throw new NotEnoughStoredException("Stored to remove was not held.");
		}
		return result;
	}
	
	@Override
	public List<ItemExpiryEvent> updateExpiredStates(ObjectId blockKey, Duration expiredWarningThreshold) {
		List<ItemExpiryEvent> events = new ArrayList<>();
		for (Map.Entry<String, S> curStored : this.entrySet()) {
			Optional<ItemExpiryEvent> result = updateExpiredStateForStored(
				curStored.getValue(),
				blockKey,
				expiredWarningThreshold
			);
			
			if (result.isPresent()) {
				events.add(
					(ItemExpiryEvent) result.get().setIdentifier(curStored.getKey())
				);
			}
		}
		return events;
	}
	
	// <editor-fold desc="Map pass-through methods">
	public int size() {
		return this.getStored().size();
	}
	
	@JsonIgnore
	public boolean isEmpty() {
		return this.getStored().isEmpty();
	}
	
	public boolean containsKey(Object o) {
		return this.getStored().containsKey(o);
	}
	
	public boolean containsValue(Object o) {
		return this.getStored().containsValue(o);
	}
	
	@JsonIgnore
	public S get(Object o) {
		return this.getStored().get(o);
	}
	
	public S put(String s, S s2) {
		S output = this.getStored().put(s, s2);
		this.recalcTotal();
		return output;
	}
	
	public S remove(Object o) {
		S output = this.getStored().remove(o);
		this.recalcTotal();
		return output;
	}
	
	public void putAll(Map<? extends String, ? extends S> map) {
		this.getStored().putAll(map);
		this.recalcTotal();
	}
	
	public void clear() {
		this.getStored().clear();
		this.recalcTotal();
	}
	
	public Set<String> keySet() {
		return this.getStored().keySet();
	}
	
	public Collection<S> values() {
		return this.getStored().values();
	}
	
	public Set<Map.Entry<String, S>> entrySet() {
		return this.getStored().entrySet();
	}
	
	@JsonIgnore
	public S getOrDefault(Object key, S defaultValue) {
		S output = this.getStored().getOrDefault(key, defaultValue);
		this.recalcTotal();
		return output;
	}
	
	public void forEach(BiConsumer<? super String, ? super S> action) {
		this.getStored().forEach(action);
	}
	
	public void replaceAll(BiFunction<? super String, ? super S, ? extends S> function) {
		this.getStored().replaceAll(function);
		this.recalcTotal();
	}
	
	public S putIfAbsent(String key, S value) {
		S output = this.getStored().putIfAbsent(key, value);
		this.recalcTotal();
		return output;
	}
	
	public boolean remove(Object key, Object value) {
		boolean output = this.getStored().remove(key, value);
		this.recalcTotal();
		return output;
	}
	
	public boolean replace(String key, S oldValue, S newValue) {
		boolean output = this.getStored().replace(key, oldValue, newValue);
		this.recalcTotal();
		return output;
	}
	
	public S replace(String key, S value) {
		S output = this.getStored().replace(key, value);
		this.recalcTotal();
		return output;
	}
	
	public S computeIfAbsent(String key, Function<? super String, ? extends S> mappingFunction) {
		return this.getStored().computeIfAbsent(key, mappingFunction);
	}
	
	public S computeIfPresent(String key, BiFunction<? super String, ? super S, ? extends S> remappingFunction) {
		return this.getStored().computeIfPresent(key, remappingFunction);
	}
	
	public S compute(String key, BiFunction<? super String, ? super S, ? extends S> remappingFunction) {
		return this.getStored().compute(key, remappingFunction);
	}
	
	public S merge(String key, S value, BiFunction<? super S, ? super S, ? extends S> remappingFunction) {
		return this.getStored().merge(key, value, remappingFunction);
	}
	// </editor-fold>
	
	
	@Override
	public Iterator<S> iterator() {
		return this.getStored().values().iterator();
	}
	
	@Override
	public Stream<S> stream() {
		return this.getStored().values().stream();
	}
	
	@Override
	public Stream<S> parallelStream() {
		return this.getStored().values().stream();
	}
	
	@Override
	public void forEach(Consumer<? super S> action) {
		((Collection<S>) this.getStored()).stream();
		this.getStored().values().forEach(action);
	}
}
