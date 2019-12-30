package com.lishid.orebfuscator.utils;

import com.lishid.orebfuscator.api.utils.IPair;

public class Pair<K, V> implements IPair<K, V> {

	private final K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public K getKey() {
		return this.key;
	}

	@Override
	public V getValue() {
		return this.value;
	}

	@Override
	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;

		return oldValue;
	}
}