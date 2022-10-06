package tech.ebp.oqm.lib.core.object.storage.items.utils;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.stream.Stream;

public abstract class SumHelper<V> {
	
	@Getter
	@Setter(value = AccessLevel.PROTECTED)
	private V total;
	
	protected SumHelper(V zeroValue) {
		this.total = zeroValue;
	}
	
	public abstract void add(V val);
	
	public void addAll(Stream<V> stream) {
		stream.forEach(this::add);
	}
	
	public void addAll(V... vs) {
		for (V v : vs) {
			this.add(v);
		}
	}
	
	public void addAll(Collection<V> vs) {
		for (V v : vs) {
			this.add(v);
		}
	}
}
