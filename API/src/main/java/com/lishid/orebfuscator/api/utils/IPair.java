package com.lishid.orebfuscator.api.utils;

import java.util.Map;

public interface IPair<K, V> extends Map.Entry<K, V> {

	public K getKey();

	public V getValue();

	public V setValue(V value);
}