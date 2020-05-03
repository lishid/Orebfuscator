package net.imprex.orebfuscator.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ObjectIdMap<T> {

	private final Map<Integer, T> byKey;
	private final Map<T, Integer> byValue;

	private int index = 0;

	public ObjectIdMap() {
		this.byKey = new HashMap<>();
		this.byValue = new HashMap<>();
	}

	public ObjectIdMap(int initialCapacity) {
		this.byKey = new HashMap<>(initialCapacity);
		this.byValue = new HashMap<>(initialCapacity);
	}

	public int add(T value) {
		Objects.requireNonNull(value, "Value type can't be null");
		this.byKey.put(Integer.valueOf(this.index), value);
		this.byValue.put(value, Integer.valueOf(this.index));
		return this.index++;
	}

	public void set(int index, T value) {
		Objects.requireNonNull(value, "Class type can't be null");
		this.byKey.put(Integer.valueOf(index), value);
		this.byValue.put(value, Integer.valueOf(index));
	}

	public int getKey(T value) {
		Objects.requireNonNull(value, "Value can't be null");
		Integer id = this.byValue.get(value);
		return id == null ? -1 : id.intValue();
	}

	public T getValue(int id) {
		return this.byKey.get(Integer.valueOf(id));
	}

	public int size() {
		return this.byKey.size();
	}

	public void clear() {
		this.byKey.clear();
		this.byValue.clear();
		this.index = 0;
	}

	@Override
	public String toString() {
		return this.byKey.toString();
	}
}
